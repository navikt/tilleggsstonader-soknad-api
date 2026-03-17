package no.nav.tilleggsstonader.soknad.kjøreliste

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import no.nav.tilleggsstonader.kontrakter.felles.JsonMapperProvider.jsonMapper
import no.nav.tilleggsstonader.kontrakter.felles.Skjematype
import no.nav.tilleggsstonader.kontrakter.felles.Språkkode
import no.nav.tilleggsstonader.kontrakter.søknad.DatoFelt
import no.nav.tilleggsstonader.kontrakter.søknad.Dokument
import no.nav.tilleggsstonader.kontrakter.søknad.DokumentasjonFelt
import no.nav.tilleggsstonader.kontrakter.søknad.InnsendtSkjema
import no.nav.tilleggsstonader.kontrakter.søknad.KjørelisteSkjema
import no.nav.tilleggsstonader.kontrakter.søknad.Reisedag
import no.nav.tilleggsstonader.kontrakter.søknad.UkeMedReisedager
import no.nav.tilleggsstonader.kontrakter.søknad.Vedleggstype
import no.nav.tilleggsstonader.kontrakter.søknad.VerdiFelt
import no.nav.tilleggsstonader.libs.sikkerhet.EksternBrukerUtils
import no.nav.tilleggsstonader.soknad.infrastruktur.database.JsonWrapper
import no.nav.tilleggsstonader.soknad.kjøreliste.KjørelisteTestdata.datofelt
import no.nav.tilleggsstonader.soknad.kjøreliste.KjørelisteTestdata.parkeringsutgift
import no.nav.tilleggsstonader.soknad.sak.DagligReisePrivatBilClient
import no.nav.tilleggsstonader.soknad.soknad.SkjemaService
import no.nav.tilleggsstonader.soknad.soknad.SøknadMetadataDto
import no.nav.tilleggsstonader.soknad.soknad.SøknadValideringException
import no.nav.tilleggsstonader.soknad.soknad.domene.Skjema
import no.nav.tilleggsstonader.soknad.soknad.domene.SkjemaRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class KjørelisteServiceTest {
    private val skjemaService = mockk<SkjemaService>()
    private val dagligReisePrivatBilClient = mockk<DagligReisePrivatBilClient>()
    private val skjemaRepository = mockk<SkjemaRepository>()

    private val service =
        KjørelisteService(
            skjemaService = skjemaService,
            dagligReisePrivatBilClient = dagligReisePrivatBilClient,
            skjemaRepository = skjemaRepository,
        )

    private val personIdent = "12345678901"

    @BeforeEach
    fun setUp() {
        mockkObject(EksternBrukerUtils)
        every { EksternBrukerUtils.hentFnrFraToken() } returns personIdent
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(EksternBrukerUtils)
    }

    @Nested
    inner class ValiderKjøreliste {
        @Test
        fun `skal ikke kaste feil når det ikke finnes tidligere innsendte kjørelister`() {
            every {
                skjemaRepository.findByPersonIdentAndType(
                    personIdent,
                    Skjematype.DAGLIG_REISE_KJØRELISTE,
                )
            } returns emptyList()

            val dto =
                lagKjørelisteDto(
                    reiseId = "reise-1",
                    uker =
                        listOf(
                            lagUkeMedReisedagerDto(
                                "Uke 23 (2. juni - 8. juni)",
                                LocalDate.of(2025, 6, 2),
                                LocalDate.of(2025, 6, 3),
                            ),
                        ),
                )

            assertDoesNotThrow { service.validerKjøreliste(dto) }
        }

        @Test
        fun `skal ikke kaste feil når tidligere kjøreliste har uker i en annen uke`() {
            val reiseId = "reise-1"
            val tidligereKjøreliste =
                lagLagretSkjema(
                    reiseId = reiseId,
                    uker =
                        listOf(
                            lagUkeMedReisedagerDto(
                                "Uke 23 (2. juni - 8. juni)",
                                LocalDate.of(2025, 6, 2),
                                LocalDate.of(2025, 6, 3),
                            ),
                        ),
                )
            every { skjemaRepository.findByPersonIdentAndType(personIdent, Skjematype.DAGLIG_REISE_KJØRELISTE) } returns
                listOf(tidligereKjøreliste)

            val dto =
                lagKjørelisteDto(
                    reiseId = reiseId,
                    uker =
                        listOf(
                            lagUkeMedReisedagerDto(
                                "Uke 24 (9. juni - 15. juni)",
                                LocalDate.of(2025, 6, 9),
                                LocalDate.of(2025, 6, 10),
                            ),
                        ),
                )

            assertDoesNotThrow { service.validerKjøreliste(dto) }
        }

        @Test
        fun `skal kaste feil når uke allerede er sendt inn for samme reiseId`() {
            val reiseId = "reise-1"
            val ukeLabel = "Uke 23 (2. juni - 8. juni)"
            val tidligereKjøreliste =
                lagLagretSkjema(
                    reiseId = reiseId,
                    uker = listOf(lagUkeMedReisedagerDto(ukeLabel, LocalDate.of(2025, 6, 2), LocalDate.of(2025, 6, 3))),
                )
            every { skjemaRepository.findByPersonIdentAndType(personIdent, Skjematype.DAGLIG_REISE_KJØRELISTE) } returns
                listOf(tidligereKjøreliste)

            val dto =
                lagKjørelisteDto(
                    reiseId = reiseId,
                    uker = listOf(lagUkeMedReisedagerDto(ukeLabel, LocalDate.of(2025, 6, 2), LocalDate.of(2025, 6, 3))),
                )

            assertThatThrownBy { service.validerKjøreliste(dto) }
                .isInstanceOf(SøknadValideringException::class.java)
                .hasMessage("$ukeLabel er allerede sendt inn. Kan ikke sende inn på nytt")
        }

        @Test
        fun `skal kaste feil når andre dager i samme uke sendes inn`() {
            val reiseId = "reise-1"
            val ukeLabel = "Uke 23 (2. juni - 8. juni)"
            // Tidligere innsendt mandag og tirsdag
            val tidligereKjøreliste =
                lagLagretSkjema(
                    reiseId = reiseId,
                    uker = listOf(lagUkeMedReisedagerDto(ukeLabel, LocalDate.of(2025, 6, 2), LocalDate.of(2025, 6, 3))),
                )
            every { skjemaRepository.findByPersonIdentAndType(personIdent, Skjematype.DAGLIG_REISE_KJØRELISTE) } returns
                listOf(tidligereKjøreliste)

            // Ny innsending med torsdag og fredag i samme uke
            val dto =
                lagKjørelisteDto(
                    reiseId = reiseId,
                    uker = listOf(lagUkeMedReisedagerDto(ukeLabel, LocalDate.of(2025, 6, 5), LocalDate.of(2025, 6, 6))),
                )

            assertThatThrownBy { service.validerKjøreliste(dto) }
                .isInstanceOf(SøknadValideringException::class.java)
                .hasMessage("$ukeLabel er allerede sendt inn. Kan ikke sende inn på nytt")
        }

        @Test
        fun `skal ikke kaste feil når samme uke er sendt inn for annen reiseId`() {
            val tidligereKjøreliste =
                lagLagretSkjema(
                    reiseId = "reise-1",
                    uker =
                        listOf(
                            lagUkeMedReisedagerDto(
                                "Uke 23 (2. juni - 8. juni)",
                                LocalDate.of(2025, 6, 2),
                                LocalDate.of(2025, 6, 3),
                            ),
                        ),
                )
            every { skjemaRepository.findByPersonIdentAndType(personIdent, Skjematype.DAGLIG_REISE_KJØRELISTE) } returns
                listOf(tidligereKjøreliste)

            val dto =
                lagKjørelisteDto(
                    reiseId = "reise-2",
                    uker =
                        listOf(
                            lagUkeMedReisedagerDto(
                                "Uke 23 (2. juni - 8. juni)",
                                LocalDate.of(2025, 6, 2),
                                LocalDate.of(2025, 6, 3),
                            ),
                        ),
                )

            assertDoesNotThrow { service.validerKjøreliste(dto) }
        }

        @Test
        fun `skal kaste feil på første duplikat-uke`() {
            val reiseId = "reise-1"
            val tidligereKjøreliste =
                lagLagretSkjema(
                    reiseId = reiseId,
                    uker =
                        listOf(
                            lagUkeMedReisedagerDto(
                                "Uke 23 (2. juni - 8. juni)",
                                LocalDate.of(2025, 6, 2),
                                LocalDate.of(2025, 6, 3),
                            ),
                            lagUkeMedReisedagerDto(
                                "Uke 24 (9. juni - 15. juni)",
                                LocalDate.of(2025, 6, 9),
                                LocalDate.of(2025, 6, 10),
                            ),
                        ),
                )
            every { skjemaRepository.findByPersonIdentAndType(personIdent, Skjematype.DAGLIG_REISE_KJØRELISTE) } returns
                listOf(tidligereKjøreliste)

            val dto =
                lagKjørelisteDto(
                    reiseId = reiseId,
                    uker =
                        listOf(
                            lagUkeMedReisedagerDto(
                                "Uke 23 (2. juni - 8. juni)",
                                LocalDate.of(2025, 6, 2),
                                LocalDate.of(2025, 6, 3),
                            ),
                            lagUkeMedReisedagerDto(
                                "Uke 24 (9. juni - 15. juni)",
                                LocalDate.of(2025, 6, 9),
                                LocalDate.of(2025, 6, 10),
                            ),
                        ),
                )

            assertThatThrownBy { service.validerKjøreliste(dto) }
                .isInstanceOf(SøknadValideringException::class.java)
                .hasMessage("Uke 23 (2. juni - 8. juni) er allerede sendt inn. Kan ikke sende inn på nytt")
        }

        @Test
        fun `skal kaste feil med riktig ukeLabel når kun andre uke overlapper`() {
            val reiseId = "reise-1"
            val tidligereKjøreliste =
                lagLagretSkjema(
                    reiseId = reiseId,
                    uker =
                        listOf(
                            lagUkeMedReisedagerDto(
                                "Uke 24 (9. juni - 15. juni)",
                                LocalDate.of(2025, 6, 9),
                                LocalDate.of(2025, 6, 10),
                            ),
                        ),
                )
            every { skjemaRepository.findByPersonIdentAndType(personIdent, Skjematype.DAGLIG_REISE_KJØRELISTE) } returns
                listOf(tidligereKjøreliste)

            val dto =
                lagKjørelisteDto(
                    reiseId = reiseId,
                    uker =
                        listOf(
                            lagUkeMedReisedagerDto(
                                "Uke 23 (2. juni - 8. juni)",
                                LocalDate.of(2025, 6, 2),
                                LocalDate.of(2025, 6, 3),
                            ),
                            lagUkeMedReisedagerDto(
                                "Uke 24 (9. juni - 15. juni)",
                                LocalDate.of(2025, 6, 11),
                                LocalDate.of(2025, 6, 12),
                            ),
                        ),
                )

            assertThatThrownBy { service.validerKjøreliste(dto) }
                .isInstanceOf(SøknadValideringException::class.java)
                .hasMessage("Uke 24 (9. juni - 15. juni) er allerede sendt inn. Kan ikke sende inn på nytt")
        }

        @Test
        fun `skal kaste feil når uke overlapper med en av flere tidligere innsendte kjørelister`() {
            val reiseId = "reise-1"
            val tidligereKjøreliste1 =
                lagLagretSkjema(
                    reiseId = reiseId,
                    uker =
                        listOf(
                            lagUkeMedReisedagerDto(
                                "Uke 23 (2. juni - 8. juni)",
                                LocalDate.of(2025, 6, 2),
                                LocalDate.of(2025, 6, 3),
                            ),
                        ),
                )
            val tidligereKjøreliste2 =
                lagLagretSkjema(
                    reiseId = reiseId,
                    uker =
                        listOf(
                            lagUkeMedReisedagerDto(
                                "Uke 24 (9. juni - 15. juni)",
                                LocalDate.of(2025, 6, 9),
                                LocalDate.of(2025, 6, 10),
                            ),
                        ),
                )
            every { skjemaRepository.findByPersonIdentAndType(personIdent, Skjematype.DAGLIG_REISE_KJØRELISTE) } returns
                listOf(tidligereKjøreliste1, tidligereKjøreliste2)

            val dto =
                lagKjørelisteDto(
                    reiseId = reiseId,
                    uker =
                        listOf(
                            lagUkeMedReisedagerDto(
                                "Uke 24 (9. juni - 15. juni)",
                                LocalDate.of(2025, 6, 11),
                                LocalDate.of(2025, 6, 12),
                            ),
                        ),
                )

            assertThatThrownBy { service.validerKjøreliste(dto) }
                .isInstanceOf(SøknadValideringException::class.java)
                .hasMessage("Uke 24 (9. juni - 15. juni) er allerede sendt inn. Kan ikke sende inn på nytt")
        }

        @Test
        fun `skal ikke kaste feil når kjøreliste har tom ukeliste`() {
            every {
                skjemaRepository.findByPersonIdentAndType(
                    personIdent,
                    Skjematype.DAGLIG_REISE_KJØRELISTE,
                )
            } returns emptyList()

            val dto =
                lagKjørelisteDto(
                    reiseId = "reise-1",
                    uker = emptyList(),
                )

            assertDoesNotThrow { service.validerKjøreliste(dto) }
        }

        private fun lagKjørelisteDto(
            reiseId: String,
            uker: List<UkeMedReisedagerDto>,
        ) = KjørelisteDto(
            reiseId = reiseId,
            reisedagerPerUkeAvsnitt = uker,
            dokumentasjon = emptyList(),
            søknadMetadata = SøknadMetadataDto(søknadFrontendGitHash = "abc"),
        )

        private fun lagUkeMedReisedagerDto(
            ukeLabel: String,
            vararg datoer: LocalDate,
        ) = UkeMedReisedagerDto(
            ukeLabel = ukeLabel,
            spørsmål = "Hvilke dager kjørte du?",
            reisedager =
                datoer.map {
                    ReisedagDto(
                        datofelt(it),
                        harKjørt = true,
                        parkeringsutgift = parkeringsutgift(50),
                    )
                },
        )

        private fun lagLagretSkjema(
            reiseId: String,
            uker: List<UkeMedReisedagerDto>,
        ): Skjema {
            val dto = lagKjørelisteDto(reiseId, uker)
            val innsendtSkjema = KjørelisteMapper.map(personIdent, LocalDateTime.now(), dto)
            return Skjema(
                type = Skjematype.DAGLIG_REISE_KJØRELISTE,
                personIdent = personIdent,
                skjemaJson = JsonWrapper(jsonMapper.writeValueAsString(innsendtSkjema)),
                frontendGitHash = "abc",
            )
        }
    }

    @Nested
    inner class HentKjørelisterForReise {
        @Test
        fun `skal returnere null når ingen kjørelister finnes`() {
            every {
                skjemaService.hentSkjemaerForBruker(
                    personIdent = personIdent,
                    type = Skjematype.DAGLIG_REISE_KJØRELISTE,
                )
            } returns emptyList()

            val resultat = service.hentKjørelisterForReise(reiseId = "reise-1")

            assertThat(resultat).isNull()
        }

        @Test
        fun `skal returnere null når ingen kjørelister matcher reiseId`() {
            every {
                skjemaService.hentSkjemaerForBruker(
                    personIdent = personIdent,
                    type = Skjematype.DAGLIG_REISE_KJØRELISTE,
                )
            } returns listOf(lagSkjema(reiseId = "annen-reise", uker = listOf(lagUkeMedReisedager("Uke 1"))))

            val resultat = service.hentKjørelisterForReise(reiseId = "reise-1")

            assertThat(resultat).isNull()
        }

        @Test
        fun `skal returnere kjøreliste når én matcher`() {
            val reiseId = "reise-1"
            val uke = lagUkeMedReisedager("Uke 1")

            every {
                skjemaService.hentSkjemaerForBruker(
                    personIdent = personIdent,
                    type = Skjematype.DAGLIG_REISE_KJØRELISTE,
                )
            } returns listOf(lagSkjema(reiseId = reiseId, uker = listOf(uke)))

            val resultat = service.hentKjørelisterForReise(reiseId = reiseId)

            assertThat(resultat).isNotNull
            assertThat(resultat!!.reiseId).isEqualTo(reiseId)
            assertThat(resultat.reisedagerPerUkeAvsnitt).hasSize(1)
            assertThat(resultat.reisedagerPerUkeAvsnitt[0].ukeLabel).isEqualTo("Uke 1")
        }

        @Test
        fun `skal slå sammen reisedagerPerUkeAvsnitt fra flere kjørelister`() {
            val reiseId = "reise-1"
            val uke1 = lagUkeMedReisedager("Uke 1")
            val uke2 = lagUkeMedReisedager("Uke 2")
            val uke3 = lagUkeMedReisedager("Uke 3")

            every {
                skjemaService.hentSkjemaerForBruker(
                    personIdent = personIdent,
                    type = Skjematype.DAGLIG_REISE_KJØRELISTE,
                )
            } returns
                listOf(
                    lagSkjema(reiseId = reiseId, uker = listOf(uke1, uke2)),
                    lagSkjema(reiseId = reiseId, uker = listOf(uke3)),
                )

            val resultat = service.hentKjørelisterForReise(reiseId = reiseId)

            assertThat(resultat).isNotNull
            assertThat(resultat!!.reisedagerPerUkeAvsnitt).hasSize(3)
            assertThat(resultat.reisedagerPerUkeAvsnitt.map { it.ukeLabel })
                .containsExactly("Uke 1", "Uke 2", "Uke 3")
        }

        @Test
        fun `skal slå sammen dokumentasjon fra flere kjørelister`() {
            val reiseId = "reise-1"
            val dok1 = lagDokumentasjonFelt("Kvittering 1")
            val dok2 = lagDokumentasjonFelt("Kvittering 2")

            every {
                skjemaService.hentSkjemaerForBruker(
                    personIdent = personIdent,
                    type = Skjematype.DAGLIG_REISE_KJØRELISTE,
                )
            } returns
                listOf(
                    lagSkjema(reiseId = reiseId, dokumentasjon = listOf(dok1)),
                    lagSkjema(reiseId = reiseId, dokumentasjon = listOf(dok2)),
                )

            val resultat = service.hentKjørelisterForReise(reiseId = reiseId)

            assertThat(resultat).isNotNull
            assertThat(resultat!!.dokumentasjon).hasSize(2)
            assertThat(resultat.dokumentasjon.map { it.label })
                .containsExactly("Kvittering 1", "Kvittering 2")
        }

        @Test
        fun `skal filtrere bort kjørelister med annen reiseId`() {
            val reiseId = "reise-1"
            val ukeForRiktigReise = lagUkeMedReisedager("Uke riktig")
            val ukeForAnnenReise = lagUkeMedReisedager("Uke annen")

            every {
                skjemaService.hentSkjemaerForBruker(
                    personIdent = personIdent,
                    type = Skjematype.DAGLIG_REISE_KJØRELISTE,
                )
            } returns
                listOf(
                    lagSkjema(reiseId = reiseId, uker = listOf(ukeForRiktigReise)),
                    lagSkjema(reiseId = "annen-reise", uker = listOf(ukeForAnnenReise)),
                )

            val resultat = service.hentKjørelisterForReise(reiseId = reiseId)

            assertThat(resultat).isNotNull
            assertThat(resultat!!.reisedagerPerUkeAvsnitt).hasSize(1)
            assertThat(resultat.reisedagerPerUkeAvsnitt[0].ukeLabel).isEqualTo("Uke riktig")
        }

        @Test
        fun `søknadMetadata skal alltid ha søknadFrontendGitHash lik null`() {
            val reiseId = "reise-1"

            every {
                skjemaService.hentSkjemaerForBruker(
                    personIdent = personIdent,
                    type = Skjematype.DAGLIG_REISE_KJØRELISTE,
                )
            } returns
                listOf(
                    lagSkjema(reiseId = reiseId, frontendGitHash = "abc123"),
                    lagSkjema(reiseId = reiseId, frontendGitHash = "def456"),
                )

            val resultat = service.hentKjørelisterForReise(reiseId = reiseId)

            assertThat(resultat).isNotNull
            assertThat(resultat!!.søknadMetadata.søknadFrontendGitHash).isNull()
        }

        private fun lagSkjema(
            reiseId: String,
            uker: List<UkeMedReisedager> = listOf(lagUkeMedReisedager("Uke 1")),
            dokumentasjon: List<DokumentasjonFelt> = emptyList(),
            frontendGitHash: String? = null,
        ): Skjema {
            val kjørelisteSkjema =
                KjørelisteSkjema(
                    reiseId = reiseId,
                    reisedagerPerUkeAvsnitt = uker,
                    dokumentasjon = dokumentasjon,
                )
            val innsendtSkjema =
                InnsendtSkjema(
                    ident = personIdent,
                    mottattTidspunkt = LocalDateTime.now(),
                    språk = Språkkode.NB,
                    skjema = kjørelisteSkjema,
                )
            return Skjema(
                id = UUID.randomUUID(),
                type = Skjematype.DAGLIG_REISE_KJØRELISTE,
                personIdent = personIdent,
                skjemaJson = JsonWrapper(jsonMapper.writeValueAsString(innsendtSkjema)),
                frontendGitHash = frontendGitHash,
            )
        }

        private fun lagUkeMedReisedager(label: String): UkeMedReisedager =
            UkeMedReisedager(
                ukeLabel = label,
                spørsmål = "Hvilke dager kjørte du?",
                reisedager =
                    listOf(
                        Reisedag(
                            dato = DatoFelt(label = "Mandag 1. juni 2025", verdi = LocalDate.of(2025, 6, 1)),
                            harKjørt = true,
                            parkeringsutgift = VerdiFelt(label = "Parkeringsutgift (kr)", verdi = 50),
                        ),
                    ),
            )

        private fun lagDokumentasjonFelt(label: String): DokumentasjonFelt =
            DokumentasjonFelt(
                type = Vedleggstype.PARKERINGSUTGIFT,
                label = label,
                opplastedeVedlegg =
                    listOf(
                        Dokument(id = UUID.randomUUID(), navn = "vedlegg.pdf"),
                    ),
            )
    }
}
