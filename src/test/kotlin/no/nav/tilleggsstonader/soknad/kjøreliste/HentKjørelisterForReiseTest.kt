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
        } returns listOf(lagSkjema(reiseId = reiseId, uker = listOf(lagUkeMedReisedager("Uke 1"))))

        val resultat = service.hentKjørelisterForReise(reiseId = reiseId)

        assertThat(resultat).isNotNull
        assertThat(resultat!!.reiseId).isEqualTo(reiseId)
        assertThat(resultat.reisedagerPerUkeAvsnitt).hasSize(1)
    }

    @Test
    fun `skal slå sammen uker fra flere kjørelister`() {
        val reiseId = "reise-1"
        every {
            skjemaService.hentSkjemaerForBruker(personIdent = personIdent, type = Skjematype.DAGLIG_REISE_KJØRELISTE)
        } returns
            listOf(
                lagSkjema(reiseId = reiseId, uker = listOf(lagUkeMedReisedager("Uke 1"))),
                lagSkjema(reiseId = reiseId, uker = listOf(lagUkeMedReisedager("Uke 2"))),
            )

        val resultat = service.hentKjørelisterForReise(reiseId = reiseId)

        assertThat(resultat!!.reisedagerPerUkeAvsnitt).hasSize(2)
        assertThat(resultat.reisedagerPerUkeAvsnitt.map { it.ukeLabel }).containsExactly("Uke 1", "Uke 2")
    }

    @Test
    fun `skal filtrere bort kjørelister med annen reiseId`() {
        val reiseId = "reise-1"
        every {
            skjemaService.hentSkjemaerForBruker(personIdent = personIdent, type = Skjematype.DAGLIG_REISE_KJØRELISTE)
        } returns
            listOf(
                lagSkjema(reiseId = reiseId, uker = listOf(lagUkeMedReisedager("Uke riktig"))),
                lagSkjema(reiseId = "annen-reise", uker = listOf(lagUkeMedReisedager("Uke annen"))),
            )

        val resultat = service.hentKjørelisterForReise(reiseId = reiseId)

        assertThat(resultat!!.reisedagerPerUkeAvsnitt).hasSize(1)
        assertThat(resultat.reisedagerPerUkeAvsnitt[0].ukeLabel).isEqualTo("Uke riktig")
    }

    private fun lagSkjema(
        reiseId: String,
        uker: List<UkeMedReisedager> = listOf(lagUkeMedReisedager("Uke 1")),
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

    private fun lagUkeMedReisedager(label: String): UkeMedReisedager =
        UkeMedReisedager(
            ukeLabel = label,
            reisedagerLabel = "Ukentlige reisedager: 3",
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
}
