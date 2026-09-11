package no.nav.tilleggsstonader.soknad.kjøreliste

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.tilleggsstonader.kontrakter.felles.Skjematype
import no.nav.tilleggsstonader.libs.sikkerhet.EksternBrukerUtils
import no.nav.tilleggsstonader.soknad.IntegrationTest
import no.nav.tilleggsstonader.soknad.infrastruktur.DagligReisePrivatBilClientConfig
import no.nav.tilleggsstonader.soknad.sak.DagligReisePrivatBilClient
import no.nav.tilleggsstonader.soknad.soknad.domene.SkjemaRepository
import no.nav.tilleggsstonader.soknad.tokenSubject
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessResourceFailureException
import org.springframework.jdbc.core.RowCallbackHandler
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.jdbc.datasource.SingleConnectionDataSource
import org.springframework.transaction.IllegalTransactionStateException
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.sql.SQLException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import javax.sql.DataSource

class KjørelisteLåsRepositoryTest
    @Autowired
    constructor(
        private val repository: KjørelisteLåsRepository,
        private val jdbcOperations: NamedParameterJdbcOperations,
        private val transactionManager: PlatformTransactionManager,
        private val dataSource: DataSource,
        private val kjørelisteService: KjørelisteService,
        private val skjemaRepository: SkjemaRepository,
        private val dagligReisePrivatBilClient: DagligReisePrivatBilClient,
    ) : IntegrationTest() {
        @Test
        fun `skal ha stabil versjonert SHA256 nøkkel med UTF8 og big endian`() {
            assertThat(KjørelisteLåsRepository.låsenøkkelForBruker(tokenSubject)).isEqualTo(-5637967242004557657L)
            assertThat(KjørelisteLåsRepository.låsenøkkelForBruker(tokenSubject)).isEqualTo(
                KjørelisteLåsRepository.låsenøkkelForBruker(tokenSubject),
            )
            assertThat(KjørelisteLåsRepository.låsenøkkelForBruker("bruker-æøå")).isEqualTo(8553774080984684991L)
        }

        @ParameterizedTest
        @ValueSource(booleans = [false, true])
        fun `skal blokkere samme bruker til commit eller rollback mens en annen bruker kan kjøre`(rollback: Boolean) {
            medKjørelisteExecutor { executor ->
                lateinit var ventende: Future<*>
                TransactionTemplate(transactionManager).executeWithoutResult { status ->
                    repository.låsBruker(tokenSubject)
                    assertThat(jdbcOperations.innstilling("transaction_isolation")).isEqualTo("read committed")
                    assertThat(jdbcOperations.antallKjørelisteLåser(tokenSubject, innvilget = true)).isEqualTo(1)

                    ventende =
                        executor.submit {
                            TransactionTemplate(transactionManager).executeWithoutResult {
                                repository.låsBruker(tokenSubject)
                            }
                        }
                    jdbcOperations.ventPåVentendeKjørelisteLås(tokenSubject)
                    assertThat(ventende.isDone).isFalse()

                    executor
                        .submit {
                            TransactionTemplate(transactionManager).executeWithoutResult {
                                repository.låsBruker("annen-bruker")
                                assertThat(jdbcOperations.antallKjørelisteLåser("annen-bruker", innvilget = true)).isEqualTo(1)
                            }
                        }.get(3, TimeUnit.SECONDS)
                    assertThat(ventende.isDone).isFalse()
                    if (rollback) status.setRollbackOnly()
                }
                ventende.get(10, TimeUnit.SECONDS)
                assertThat(jdbcOperations.antallKjørelisteLåser(tokenSubject, innvilget = true)).isZero()
                assertThat(jdbcOperations.antallKjørelisteLåser(tokenSubject, innvilget = false)).isZero()
            }
        }

        @Test
        fun `skal avvise låsing uten transaksjon`() {
            assertThatThrownBy { repository.låsBruker(tokenSubject) }
                .isInstanceOf(IllegalTransactionStateException::class.java)
            assertThat(jdbcOperations.antallKjørelisteLåser(tokenSubject, innvilget = true)).isZero()
        }

        @ParameterizedTest
        @ValueSource(ints = [0, -1])
        fun `skal kreve positiv låsetimeout`(timeout: Int) {
            assertThatThrownBy { KjørelisteLåsRepository(jdbcOperations, timeout) }
                .isInstanceOf(IllegalArgumentException::class.java)
        }

        @ParameterizedTest
        @ValueSource(booleans = [false, true])
        fun `skal gjenopprette tidligere timeout etter låsing og ved gjenbruk av samme forbindelse`(rollback: Boolean) {
            dataSource.connection.use { forbindelse ->
                val fastDataSource = SingleConnectionDataSource(forbindelse, true)
                val jdbc = NamedParameterJdbcTemplate(fastDataSource)
                val opprinneligTimeout = jdbc.innstilling("lock_timeout")
                val låsRepository = KjørelisteLåsRepository(jdbc, 1)

                TransactionTemplate(DataSourceTransactionManager(fastDataSource)).executeWithoutResult { status ->
                    jdbc.queryForObject(
                        "SELECT set_config('lock_timeout', :timeout, true)",
                        mapOf("timeout" to "17s"),
                        String::class.java,
                    )
                    låsRepository.låsBruker(tokenSubject)
                    assertThat(jdbc.innstilling("lock_timeout")).isEqualTo("17s")
                    if (rollback) status.setRollbackOnly()
                }

                assertThat(jdbc.innstilling("lock_timeout")).isEqualTo(opprinneligTimeout)
                assertThat(jdbc.antallKjørelisteLåser(tokenSubject, innvilget = true)).isZero()
            }
        }

        @Test
        fun `skal oversette reell låsetimeout og tilbakestille timeout på samme forbindelse etter rollback`() {
            medKjørelisteExecutor { executor ->
                dataSource.connection.use { forbindelse ->
                    val fastDataSource = SingleConnectionDataSource(forbindelse, true)
                    val jdbc = NamedParameterJdbcTemplate(fastDataSource)
                    val opprinneligTimeout = jdbc.innstilling("lock_timeout")
                    val låsRepository = KjørelisteLåsRepository(jdbc, 1)
                    val transaksjon = TransactionTemplate(DataSourceTransactionManager(fastDataSource))

                    TransactionTemplate(transactionManager).executeWithoutResult {
                        repository.låsBruker(tokenSubject)
                        val ventende =
                            executor.submit {
                                val exception =
                                    assertThrows<KjørelisteInnsendingPågårException> {
                                        transaksjon.executeWithoutResult { låsRepository.låsBruker(tokenSubject) }
                                    }
                                val sqlException =
                                    generateSequence<Throwable>(exception) { it.cause }.filterIsInstance<SQLException>().last()
                                assertThat(sqlException.sqlState).isEqualTo("55P03")
                            }
                        jdbcOperations.ventPåVentendeKjørelisteLås(tokenSubject)
                        ventende.get(5, TimeUnit.SECONDS)
                    }

                    assertThat(jdbc.innstilling("lock_timeout")).isEqualTo(opprinneligTimeout)
                    transaksjon.executeWithoutResult { låsRepository.låsBruker(tokenSubject) }
                }
            }
        }

        @Test
        fun `skal rulle tilbake skjema vedlegg og tasks sammen med innsendingstransaksjonen`() {
            DagligReisePrivatBilClientConfig.resetMock(dagligReisePrivatBilClient)
            mockkObject(EksternBrukerUtils)
            every { EksternBrukerUtils.hentFnrFraToken() } returns tokenSubject
            val feil = IllegalStateException("Fremprovosert feil etter lagring")
            try {
                medKjørelisteExecutor { executor ->
                    assertThatThrownBy {
                        TransactionTemplate(transactionManager).executeWithoutResult {
                            kjørelisteService.mottaKjøreliste(KjørelisteTestdata.kjørelisteDto())
                            assertThat(
                                skjemaRepository.findByPersonIdentAndType(tokenSubject, Skjematype.DAGLIG_REISE_KJØRELISTE),
                            ).hasSize(1)
                            assertThat(taskService.findAll()).hasSize(2)
                            assertThat(jdbcOperations.antallVedlegg()).isEqualTo(1)
                            assertThat(jdbcOperations.antallKjørelisteLåser(tokenSubject, innvilget = true)).isEqualTo(1)
                            executor
                                .submit {
                                    assertThat(
                                        skjemaRepository.findByPersonIdentAndType(tokenSubject, Skjematype.DAGLIG_REISE_KJØRELISTE),
                                    ).isEmpty()
                                    assertThat(taskService.findAll()).isEmpty()
                                }.get(5, TimeUnit.SECONDS)
                            throw feil
                        }
                    }.isSameAs(feil)
                }

                assertThat(skjemaRepository.findByPersonIdentAndType(tokenSubject, Skjematype.DAGLIG_REISE_KJØRELISTE)).isEmpty()
                assertThat(taskService.findAll()).isEmpty()
                assertThat(jdbcOperations.antallVedlegg()).isZero()
                assertThat(jdbcOperations.antallKjørelisteLåser(tokenSubject, innvilget = true)).isZero()

                kjørelisteService.mottaKjøreliste(KjørelisteTestdata.kjørelisteDto())
                assertThat(skjemaRepository.findByPersonIdentAndType(tokenSubject, Skjematype.DAGLIG_REISE_KJØRELISTE)).hasSize(1)
                assertThat(taskService.findAll()).hasSize(2)
            } finally {
                unmockkObject(EksternBrukerUtils)
            }
        }

        @ParameterizedTest
        @ValueSource(strings = ["40001", "57014", "08006"])
        fun `skal propagere andre databasefeil fra låsekallet uendret`(sqlState: String) {
            val jdbc = mockk<NamedParameterJdbcOperations>()
            val feil = DataAccessResourceFailureException("Databasefeil", SQLException("Feil", sqlState))
            every { jdbc.queryForObject(any<String>(), any<Map<String, Any>>(), String::class.java) } returns "0"
            every { jdbc.query(any<String>(), any<Map<String, Any>>(), any<RowCallbackHandler>()) } throws feil

            assertThatThrownBy { KjørelisteLåsRepository(jdbc, 5).låsBruker(tokenSubject) }.isSameAs(feil)

            verify(exactly = 1) {
                jdbc.queryForObject("SELECT set_config('lock_timeout', :timeout, true)", any<Map<String, Any>>(), String::class.java)
            }
        }

        @Test
        fun `skal ikke oversette 55P03 fra timeoutoppsett til pågående innsending`() {
            val jdbc = mockk<NamedParameterJdbcOperations>()
            val feil = DataAccessResourceFailureException("Databasefeil", SQLException("Feil", "55P03"))
            every { jdbc.queryForObject("SELECT current_setting('lock_timeout')", any<Map<String, Any>>(), String::class.java) } returns "0"
            every {
                jdbc.queryForObject("SELECT set_config('lock_timeout', :timeout, true)", any<Map<String, Any>>(), String::class.java)
            } throws feil

            assertThatThrownBy { KjørelisteLåsRepository(jdbc, 5).låsBruker(tokenSubject) }.isSameAs(feil)
            verify(exactly = 0) { jdbc.query(any<String>(), any<Map<String, Any>>(), any<RowCallbackHandler>()) }
        }

        private fun NamedParameterJdbcOperations.innstilling(navn: String): String =
            requireNotNull(queryForObject("SELECT current_setting(:navn)", mapOf("navn" to navn), String::class.java))
    }
