package no.nav.tilleggsstonader.soknad.kjøreliste

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import no.nav.tilleggsstonader.kontrakter.felles.JsonMapperProvider.jsonMapper
import no.nav.tilleggsstonader.kontrakter.felles.Skjematype
import no.nav.tilleggsstonader.kontrakter.felles.Språkkode
import no.nav.tilleggsstonader.kontrakter.søknad.DatoFelt
import no.nav.tilleggsstonader.kontrakter.søknad.InnsendtSkjema
import no.nav.tilleggsstonader.kontrakter.søknad.KjørelisteSkjema
import no.nav.tilleggsstonader.kontrakter.søknad.Reisedag
import no.nav.tilleggsstonader.kontrakter.søknad.UkeMedReisedager
import no.nav.tilleggsstonader.kontrakter.søknad.VerdiFelt
import no.nav.tilleggsstonader.libs.sikkerhet.EksternBrukerUtils
import no.nav.tilleggsstonader.soknad.infrastruktur.database.JsonWrapper
import no.nav.tilleggsstonader.soknad.sak.DagligReisePrivatBilClient
import no.nav.tilleggsstonader.soknad.soknad.SkjemaService
import no.nav.tilleggsstonader.soknad.soknad.domene.Skjema
import no.nav.tilleggsstonader.soknad.soknad.domene.SkjemaRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class HentKjørelisterForReiseTest {
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
        every { dagligReisePrivatBilClient.hentManueltRegistrertKjørelisteForReise(any()) } returns ManueltRegistrertKjøreliste(emptyList())
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(EksternBrukerUtils)
    }

    @Test
    fun `skal returnere null når ingen kjørelister matcher reiseId`() {
        every {
            skjemaService.hentSkjemaerForBruker(personIdent = personIdent, type = Skjematype.DAGLIG_REISE_KJØRELISTE)
        } returns listOf(lagSkjema(reiseId = "annen-reise"))

        assertThat(service.hentKjørelisterForReise(reiseId = "reise-1")).isNull()
    }

    @Test
    fun `skal returnere kjøreliste når én matcher`() {
        val reiseId = "reise-1"
        every {
            skjemaService.hentSkjemaerForBruker(personIdent = personIdent, type = Skjematype.DAGLIG_REISE_KJØRELISTE)
        } returns listOf(lagSkjema(reiseId = reiseId, uker = listOf(lagUkeMedReisedager(LocalDate.of(2025, 6, 2)))))

        val resultat = service.hentKjørelisterForReise(reiseId = reiseId)

        assertThat(resultat).isNotNull
        assertThat(resultat!!.reisedager).hasSize(1)
        assertThat(resultat.reisedager[0].dato).isEqualTo(LocalDate.of(2025, 6, 2))
    }

    @Test
    fun `skal slå sammen dager fra flere kjørelister`() {
        val reiseId = "reise-1"
        every {
            skjemaService.hentSkjemaerForBruker(personIdent = personIdent, type = Skjematype.DAGLIG_REISE_KJØRELISTE)
        } returns
            listOf(
                lagSkjema(reiseId = reiseId, uker = listOf(lagUkeMedReisedager(LocalDate.of(2025, 6, 2)))),
                lagSkjema(reiseId = reiseId, uker = listOf(lagUkeMedReisedager(LocalDate.of(2025, 6, 9)))),
            )

        val resultat = service.hentKjørelisterForReise(reiseId = reiseId)

        assertThat(resultat!!.reisedager).hasSize(2)
        assertThat(resultat.reisedager.map { it.dato })
            .containsExactly(LocalDate.of(2025, 6, 2), LocalDate.of(2025, 6, 9))
    }

    @Test
    fun `skal filtrere bort kjørelister med annen reiseId`() {
        val reiseId = "reise-1"
        every {
            skjemaService.hentSkjemaerForBruker(personIdent = personIdent, type = Skjematype.DAGLIG_REISE_KJØRELISTE)
        } returns
            listOf(
                lagSkjema(reiseId = reiseId, uker = listOf(lagUkeMedReisedager(LocalDate.of(2025, 6, 2)))),
                lagSkjema(reiseId = "annen-reise", uker = listOf(lagUkeMedReisedager(LocalDate.of(2025, 6, 9)))),
            )

        val resultat = service.hentKjørelisterForReise(reiseId = reiseId)

        assertThat(resultat!!.reisedager).hasSize(1)
        assertThat(resultat.reisedager[0].dato).isEqualTo(LocalDate.of(2025, 6, 2))
    }

    @Test
    fun `skal hente kjøreliste fra sak når soknad-api DB er tom`() {
        val reiseId = "reise-1"
        every {
            skjemaService.hentSkjemaerForBruker(personIdent = personIdent, type = Skjematype.DAGLIG_REISE_KJØRELISTE)
        } returns emptyList()
        every { dagligReisePrivatBilClient.hentManueltRegistrertKjørelisteForReise(reiseId) } returns
            ManueltRegistrertKjøreliste(
                listOf(ManueltRegistrertKjørelisteDag(dato = LocalDate.of(2025, 6, 2), harKjørt = true, parkeringsutgift = null)),
            )

        val resultat = service.hentKjørelisterForReise(reiseId = reiseId)

        assertThat(resultat).isNotNull
        assertThat(resultat!!.reisedager).hasSize(1)
        assertThat(resultat.reisedager[0].harKjørt).isTrue()
    }

    @Test
    fun `skal flette dager fra soknad-api DB og sak`() {
        val reiseId = "reise-1"
        every {
            skjemaService.hentSkjemaerForBruker(personIdent = personIdent, type = Skjematype.DAGLIG_REISE_KJØRELISTE)
        } returns listOf(lagSkjema(reiseId = reiseId, uker = listOf(lagUkeMedReisedager(LocalDate.of(2025, 6, 2)))))
        every { dagligReisePrivatBilClient.hentManueltRegistrertKjørelisteForReise(reiseId) } returns
            ManueltRegistrertKjøreliste(
                listOf(ManueltRegistrertKjørelisteDag(dato = LocalDate.of(2025, 6, 9), harKjørt = true, parkeringsutgift = null)),
            )

        val resultat = service.hentKjørelisterForReise(reiseId = reiseId)

        assertThat(resultat!!.reisedager).hasSize(2)
        assertThat(resultat.reisedager.map { it.dato })
            .containsExactly(LocalDate.of(2025, 6, 2), LocalDate.of(2025, 6, 9))
    }

    @Test
    fun `sak-data skal overstyre soknad-api-data for samme dato`() {
        val reiseId = "reise-1"
        val dato = LocalDate.of(2025, 6, 2)
        every {
            skjemaService.hentSkjemaerForBruker(personIdent = personIdent, type = Skjematype.DAGLIG_REISE_KJØRELISTE)
        } returns listOf(lagSkjema(reiseId = reiseId, uker = listOf(lagUkeMedReisedager(dato, harKjørt = false))))
        every { dagligReisePrivatBilClient.hentManueltRegistrertKjørelisteForReise(reiseId) } returns
            ManueltRegistrertKjøreliste(
                listOf(ManueltRegistrertKjørelisteDag(dato = dato, harKjørt = true, parkeringsutgift = null)),
            )

        val resultat = service.hentKjørelisterForReise(reiseId = reiseId)

        assertThat(resultat!!.reisedager).hasSize(1)
        assertThat(resultat.reisedager[0].harKjørt).isTrue()
    }

    private fun lagSkjema(
        reiseId: String,
        uker: List<UkeMedReisedager> = listOf(lagUkeMedReisedager(LocalDate.of(2025, 6, 1))),
    ): Skjema {
        val innsendtSkjema =
            InnsendtSkjema(
                ident = personIdent,
                mottattTidspunkt = LocalDateTime.now(),
                språk = Språkkode.NB,
                skjema =
                    KjørelisteSkjema(
                        reiseId = reiseId,
                        reisedagerPerUkeAvsnitt = uker,
                        dokumentasjon = emptyList(),
                    ),
            )
        return Skjema(
            id = UUID.randomUUID(),
            type = Skjematype.DAGLIG_REISE_KJØRELISTE,
            personIdent = personIdent,
            skjemaJson = JsonWrapper(jsonMapper.writeValueAsString(innsendtSkjema)),
            frontendGitHash = null,
        )
    }

    private fun lagUkeMedReisedager(
        dato: LocalDate,
        harKjørt: Boolean = true,
    ): UkeMedReisedager =
        UkeMedReisedager(
            ukeLabel = "Uke ${dato.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR)}",
            reisedagerLabel = "Ukentlige reisedager: 3",
            spørsmål = "Hvilke dager kjørte du?",
            reisedager =
                listOf(
                    Reisedag(
                        dato = DatoFelt(label = "dato", verdi = dato),
                        harKjørt = harKjørt,
                        parkeringsutgift = VerdiFelt(label = "Parkeringsutgift (kr)", verdi = if (harKjørt) 50 else null),
                    ),
                ),
        )
}
