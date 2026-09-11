package no.nav.tilleggsstonader.soknad.kjøreliste

import io.mockk.every
import no.nav.tilleggsstonader.kontrakter.felles.Skjematype
import no.nav.tilleggsstonader.soknad.IntegrationTest
import no.nav.tilleggsstonader.soknad.infrastruktur.DagligReisePrivatBilClientConfig
import no.nav.tilleggsstonader.soknad.integrasjonstest.extensions.kall.hentKjørelister
import no.nav.tilleggsstonader.soknad.integrasjonstest.extensions.kall.hentKjørelisterKall
import no.nav.tilleggsstonader.soknad.prosessering.LagPdfTask
import no.nav.tilleggsstonader.soknad.prosessering.SendNotifikasjonTask
import no.nav.tilleggsstonader.soknad.sak.DagligReisePrivatBilClient
import no.nav.tilleggsstonader.soknad.soknad.domene.SkjemaRepository
import no.nav.tilleggsstonader.soknad.tokenSubject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.test.web.servlet.client.RestTestClient
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDate
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class KjørelisteControllerTest
    @Autowired
    constructor(
        private val jdbcOperations: NamedParameterJdbcOperations,
        private val transactionManager: PlatformTransactionManager,
        private val kjørelisteLåsRepository: KjørelisteLåsRepository,
    ) : IntegrationTest() {
        @Autowired
        private lateinit var dagligReisePrivatBilClient: DagligReisePrivatBilClient

        @Autowired
        private lateinit var skjemaRepository: SkjemaRepository

        @BeforeEach
        fun resetKjørelisteMock() {
            DagligReisePrivatBilClientConfig.resetMock(dagligReisePrivatBilClient)
        }

        @Test
        fun `skal returnere null når ingen kjøreliste finnes for reiseId`() {
            val result = hentKjørelister("ukjent-reise")

            assertThat(result).isNull()
        }

        @Test
        fun `skal returnere kjøreliste fra sak`() {
            val dato = LocalDate.of(2025, 1, 6)
            every { dagligReisePrivatBilClient.hentManueltRegistrertKjørelisteForReise("1") } returns
                ManueltRegistrertKjøreliste(
                    listOf(ManueltRegistrertKjørelisteDag(dato = dato, harKjørt = true, parkeringsutgift = 50)),
                )

            val result = hentKjørelister("1")

            assertThat(result).isNotNull
            assertThat(result!!.reisedager).hasSize(1)
            with(result.reisedager[0]) {
                assertThat(this.dato).isEqualTo(dato)
                assertThat(harKjørt).isTrue()
                assertThat(parkeringsutgift).isEqualTo(50)
            }
        }

        @Test
        fun `skal returnere kjøreliste sendt inn via soknad-api`() {
            val kjøreliste = KjørelisteTestdata.kjørelisteDto()

            restTestClient
                .post()
                .uri("/api/kjorelister")
                .body(kjøreliste)
                .medSøkerBearerToken(tokenSubject)
                .exchange()
                .expectStatus()
                .isOk

            val result = hentKjørelister(kjøreliste.reiseId)

            assertThat(result).isNotNull
            assertThat(result!!.reisedager).hasSize(3)
            assertThat(result.reisedager.map { it.dato }).containsExactly(
                LocalDate.of(2025, 1, 6),
                LocalDate.of(2025, 1, 7),
                LocalDate.of(2025, 1, 10),
            )
            assertThat(result.reisedager).allMatch { it.harKjørt }
            assertThat(result.reisedager.map { it.parkeringsutgift }).containsExactly(130, 40, 90)
        }

        @Test
        fun `skal avvise identisk kjøreliste når første innsending er ferdig lagret`() {
            val kjøreliste = KjørelisteTestdata.kjørelisteDto().copy(dokumentasjon = emptyList())

            restTestClient
                .post()
                .uri("/api/kjorelister")
                .body(kjøreliste)
                .medSøkerBearerToken(tokenSubject)
                .exchange()
                .expectStatus()
                .isOk

            restTestClient
                .post()
                .uri("/api/kjorelister")
                .body(kjøreliste)
                .medSøkerBearerToken(tokenSubject)
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody()
                .jsonPath("$.detail")
                .isEqualTo("Uke 2 er allerede sendt inn. Kan ikke sende inn på nytt")

            assertThat(skjemaRepository.findByPersonIdentAndType(tokenSubject, Skjematype.DAGLIG_REISE_KJØRELISTE)).hasSize(1)
            assertThat(taskService.findAll()).hasSize(2)
        }

        @Test
        fun `skal lagre bare en av to samtidige identiske kjørelister`() {
            val kjøreliste = KjørelisteTestdata.kjørelisteDto().copy(dokumentasjon = emptyList())

            sendSamtidig(kjøreliste, kjøreliste) { respons ->
                respons
                    .expectStatus()
                    .isBadRequest
                    .expectBody()
                    .jsonPath("$.detail")
                    .isEqualTo("Uke 2 er allerede sendt inn. Kan ikke sende inn på nytt")
            }

            val skjemaer = skjemaRepository.findByPersonIdentAndType(tokenSubject, Skjematype.DAGLIG_REISE_KJØRELISTE)
            assertThat(skjemaer).hasSize(1)
            assertThat(taskService.findAll().map { it.type })
                .containsExactlyInAnyOrder(LagPdfTask.TYPE, SendNotifikasjonTask.TYPE)
            assertThat(taskService.findAll()).allMatch { it.payload == skjemaer.single().id.toString() }
            assertThat(jdbcOperations.antallVedlegg()).isZero()
        }

        @Test
        fun `skal fullføre samtidige kjørelister for samme bruker med ulike uker`() {
            val første = KjørelisteTestdata.kjørelisteDto().copy(dokumentasjon = emptyList())
            val andre =
                første.copy(
                    reisedagerPerUkeAvsnitt =
                        KjørelisteTestdata.lagUkeliste(LocalDate.of(2025, 1, 13), LocalDate.of(2025, 1, 19)),
                )

            sendSamtidig(første, andre) { it.expectStatus().isOk }

            val skjemaer = skjemaRepository.findByPersonIdentAndType(tokenSubject, Skjematype.DAGLIG_REISE_KJØRELISTE)
            assertThat(skjemaer).hasSize(2)
            assertThat(taskService.findAll()).hasSize(4)
            skjemaer.forEach { skjema ->
                assertThat(taskService.findAll().filter { it.payload == skjema.id.toString() }.map { it.type })
                    .containsExactlyInAnyOrder(LagPdfTask.TYPE, SendNotifikasjonTask.TYPE)
            }
        }

        @Test
        fun `skal returnere 409 uten lagring ved låsetimeout og tillate nytt forsøk`() {
            val kjøreliste = KjørelisteTestdata.kjørelisteDto()
            val token = søkerBearerToken(tokenSubject)

            medKjørelisteExecutor { executor ->
                TransactionTemplate(transactionManager).executeWithoutResult {
                    kjørelisteLåsRepository.låsBruker(tokenSubject)
                    val innsending =
                        executor.submit {
                            sendKjøreliste(kjøreliste, token)
                                .expectStatus()
                                .isEqualTo(409)
                                .expectBody()
                                .jsonPath("$.detail")
                                .isEqualTo("En kjøreliste er allerede under innsending. Vent litt og prøv igjen.")
                        }
                    jdbcOperations.ventPåVentendeKjørelisteLås(tokenSubject)
                    innsending.get(15, TimeUnit.SECONDS)
                    assertThat(skjemaRepository.findByPersonIdentAndType(tokenSubject, Skjematype.DAGLIG_REISE_KJØRELISTE)).isEmpty()
                    assertThat(taskService.findAll()).isEmpty()
                    assertThat(jdbcOperations.antallVedlegg()).isZero()
                }
            }

            sendKjøreliste(kjøreliste, token).expectStatus().isOk
            assertThat(skjemaRepository.findByPersonIdentAndType(tokenSubject, Skjematype.DAGLIG_REISE_KJØRELISTE)).hasSize(1)
            assertThat(taskService.findAll()).hasSize(2)
        }

        private fun sendSamtidig(
            første: KjørelisteDto,
            andre: KjørelisteDto,
            sjekkAndreRespons: (RestTestClient.ResponseSpec) -> Unit,
        ) {
            val token = søkerBearerToken(tokenSubject)
            val rammevedtak = dagligReisePrivatBilClient.hentRammevedtakForInnloggetBruker()
            val førsteHarLås = CountDownLatch(1)
            val slippFørste = CountDownLatch(1)
            val førsteKall = AtomicBoolean(true)
            every { dagligReisePrivatBilClient.hentRammevedtakForInnloggetBruker() } answers {
                if (førsteKall.compareAndSet(true, false)) {
                    førsteHarLås.countDown()
                    check(slippFørste.await(15, TimeUnit.SECONDS)) { "Første innsending ble ikke sluppet" }
                }
                rammevedtak
            }

            medKjørelisteExecutor { executor ->
                try {
                    val førsteInnsending = executor.submit { sendKjøreliste(første, token).expectStatus().isOk }
                    assertThat(førsteHarLås.await(10, TimeUnit.SECONDS)).isTrue()
                    val andreInnsending = executor.submit { sjekkAndreRespons(sendKjøreliste(andre, token)) }
                    jdbcOperations.ventPåVentendeKjørelisteLås(tokenSubject)
                    slippFørste.countDown()
                    førsteInnsending.get(20, TimeUnit.SECONDS)
                    andreInnsending.get(20, TimeUnit.SECONDS)
                } finally {
                    slippFørste.countDown()
                }
            }
        }

        private fun sendKjøreliste(
            kjøreliste: KjørelisteDto,
            token: String,
        ) = restTestClient
            .post()
            .uri("/api/kjorelister")
            .body(kjøreliste)
            .headers { it.setBearerAuth(token) }
            .exchange()

        @Test
        fun `skal feile med 401 uten token`() {
            hentKjørelisterKall("1", personident = null)
                .expectStatus()
                .isUnauthorized
        }
    }
