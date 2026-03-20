package no.nav.tilleggsstonader.soknad.kjøreliste

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import no.nav.tilleggsstonader.kontrakter.felles.JsonMapperProvider.jsonMapper
import no.nav.tilleggsstonader.kontrakter.felles.Skjematype
import no.nav.tilleggsstonader.kontrakter.søknad.RammevedtakDto
import no.nav.tilleggsstonader.kontrakter.søknad.RammevedtakUkeDto
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
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

class ValiderKjørelisteTest {
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
    private val reiseId = "reise-1"

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
    fun `skal ikke kaste feil når det ikke finnes tidligere innsendte kjørelister`() {
        mockIngenTidligereInnsendinger()
        mockRammevedtak(lagRammevedtakUke(LocalDate.of(2025, 6, 2), kanSendeInn = true))

        val dto = lagKjørelisteDto(lagUke("Uke 23", LocalDate.of(2025, 6, 2)))

        assertDoesNotThrow { service.validerKjøreliste(dto) }
    }

    @Test
    fun `skal kaste feil når uke allerede er sendt inn`() {
        mockTidligereInnsendt(lagUke("Uke 23", LocalDate.of(2025, 6, 2)))
        mockRammevedtak(lagRammevedtakUke(LocalDate.of(2025, 6, 2), kanSendeInn = true))

        val dto = lagKjørelisteDto(lagUke("Uke 23", LocalDate.of(2025, 6, 2)))

        assertThatThrownBy { service.validerKjøreliste(dto) }
            .isInstanceOf(SøknadValideringException::class.java)
            .hasMessage("Uke 23 er allerede sendt inn. Kan ikke sende inn på nytt")
    }

    @Test
    fun `skal kaste feil når andre dager i samme uke sendes inn`() {
        mockTidligereInnsendt(lagUke("Uke 23", LocalDate.of(2025, 6, 2)))
        mockRammevedtak(lagRammevedtakUke(LocalDate.of(2025, 6, 2), kanSendeInn = true))

        val dto = lagKjørelisteDto(lagUke("Uke 23", LocalDate.of(2025, 6, 5)))

        assertThatThrownBy { service.validerKjøreliste(dto) }
            .isInstanceOf(SøknadValideringException::class.java)
            .hasMessage("Uke 23 er allerede sendt inn. Kan ikke sende inn på nytt")
    }

    @Test
    fun `skal ikke kaste feil når samme uke er sendt inn for annen reiseId`() {
        val tidligereKjøreliste =
            lagLagretSkjema(
                reiseId = "reise-1",
                uker = listOf(lagUke("Uke 23", LocalDate.of(2025, 6, 2))),
            )
        every { skjemaRepository.findByPersonIdentAndType(personIdent, Skjematype.DAGLIG_REISE_KJØRELISTE) } returns
            listOf(tidligereKjøreliste)
        mockRammevedtak(
            lagRammevedtakUke(LocalDate.of(2025, 6, 2), kanSendeInn = true),
            reiseId = "reise-2",
        )

        val dto = lagKjørelisteDto(lagUke("Uke 23", LocalDate.of(2025, 6, 2)), reiseId = "reise-2")

        assertDoesNotThrow { service.validerKjøreliste(dto) }
    }

    @Test
    fun `skal kaste feil når uke ikke er klar for innsending`() {
        mockIngenTidligereInnsendinger()
        mockRammevedtak(lagRammevedtakUke(LocalDate.of(2025, 6, 2), kanSendeInn = false))

        val dto = lagKjørelisteDto(lagUke("Uke 23", LocalDate.of(2025, 6, 2)))

        assertThatThrownBy { service.validerKjøreliste(dto) }
            .isInstanceOf(SøknadValideringException::class.java)
            .hasMessage("Kunne ikke sende inn kjøreliste. Uke 23 er ikke klar for innsending.")
    }

    @Test
    fun `skal kaste feil når uke har innsendtDato i rammevedtaket`() {
        mockIngenTidligereInnsendinger()
        mockRammevedtak(
            lagRammevedtakUke(LocalDate.of(2025, 6, 2), kanSendeInn = true, innsendtDato = LocalDate.of(2025, 6, 5)),
        )

        val dto = lagKjørelisteDto(lagUke("Uke 23", LocalDate.of(2025, 6, 2)))

        assertThatThrownBy { service.validerKjøreliste(dto) }
            .isInstanceOf(SøknadValideringException::class.java)
            .hasMessage("Uke 23 er allerede sendt inn. Kan ikke sende inn på nytt")
    }

    private fun lagKjørelisteDto(
        vararg uker: UkeMedReisedagerDto,
        reiseId: String = this.reiseId,
    ) = KjørelisteDto(
        reiseId = reiseId,
        reisedagerPerUkeAvsnitt = uker.toList(),
        dokumentasjon = emptyList(),
        søknadMetadata = SøknadMetadataDto(søknadFrontendGitHash = "abc"),
    )

    private fun lagUke(
        ukeLabel: String,
        vararg datoer: LocalDate,
    ) = UkeMedReisedagerDto(
        ukeLabel = ukeLabel,
        spørsmål = "Hvilke dager kjørte du?",
        reisedager = datoer.map { ReisedagDto(datofelt(it), harKjørt = true, parkeringsutgift = parkeringsutgift(50)) },
    )

    private fun mockIngenTidligereInnsendinger() {
        every { skjemaRepository.findByPersonIdentAndType(personIdent, Skjematype.DAGLIG_REISE_KJØRELISTE) } returns emptyList()
    }

    private fun mockTidligereInnsendt(vararg uker: UkeMedReisedagerDto) {
        val tidligereKjøreliste = lagLagretSkjema(reiseId = reiseId, uker = uker.toList())
        every { skjemaRepository.findByPersonIdentAndType(personIdent, Skjematype.DAGLIG_REISE_KJØRELISTE) } returns
            listOf(tidligereKjøreliste)
    }

    private fun lagLagretSkjema(
        reiseId: String,
        uker: List<UkeMedReisedagerDto>,
    ): Skjema {
        val dto =
            KjørelisteDto(
                reiseId = reiseId,
                reisedagerPerUkeAvsnitt = uker,
                dokumentasjon = emptyList(),
                søknadMetadata = SøknadMetadataDto(søknadFrontendGitHash = "abc"),
            )
        val innsendtSkjema = KjørelisteMapper.map(personIdent, LocalDateTime.now(), dto)
        return Skjema(
            type = Skjematype.DAGLIG_REISE_KJØRELISTE,
            personIdent = personIdent,
            skjemaJson = JsonWrapper(jsonMapper.writeValueAsString(innsendtSkjema)),
            frontendGitHash = "abc",
        )
    }

    private fun mockRammevedtak(
        vararg uker: RammevedtakUkeDto,
        reiseId: String = this.reiseId,
    ) {
        val rammevedtak =
            RammevedtakDto(
                reiseId = reiseId,
                fom = LocalDate.of(2025, 6, 1),
                tom = LocalDate.of(2025, 7, 1),
                reisedagerPerUke = 3,
                aktivitetsadresse = "Testveien 1",
                aktivitetsnavn = "Arbeidstrening",
                uker = uker.toList(),
            )
        every { dagligReisePrivatBilClient.hentRammevedtakForInnloggetBruker() } returns listOf(rammevedtak)
    }

    private fun lagRammevedtakUke(
        fom: LocalDate,
        kanSendeInn: Boolean,
        innsendtDato: LocalDate? = null,
    ): RammevedtakUkeDto {
        val uke = Uke(fom)
        return RammevedtakUkeDto(
            fom = uke.mandag,
            tom = uke.søndag,
            ukeNummer = 1,
            innsendtDato = innsendtDato,
            kanSendeInnKjøreliste = kanSendeInn,
        )
    }
}
