package no.nav.tilleggsstonader.soknad.dokument

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import no.nav.security.mock.oauth2.http.objectMapper
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.soknad.dokument.pdf.HtmlGenerator
import no.nav.tilleggsstonader.soknad.infrastruktur.database.JsonWrapper
import no.nav.tilleggsstonader.soknad.soknad.Søknad
import no.nav.tilleggsstonader.soknad.soknad.SøknadService
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.BarnetilsynMapper
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.SøknadBarnetilsynUtil
import no.nav.tilleggsstonader.soknad.util.FileUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDateTime

class PdfServiceTest {

    private val søknadService = mockk<SøknadService>()
    private val familieDokumentClient = mockk<FamilieDokumentClient>()
    private val pdfService = PdfService(søknadService, HtmlGenerator(prettyPrint = true), familieDokumentClient)

    val oppdaterSøknadSlot = slot<Søknad>()
    val htmlSlot = slot<String>()

    private val pdfBytes = "pdf".toByteArray()

    @BeforeEach
    fun setUp() {
        justRun { søknadService.oppdaterSøknad(capture(oppdaterSøknadSlot)) }
        every { familieDokumentClient.genererPdf(capture(htmlSlot)) } returns pdfBytes
    }

    @Test
    fun `skal lage pdf fra barnetilsyn`() {
        val søknad = lagSøknad(Stønadstype.BARNETILSYN, BarnetilsynMapper().map(SøknadBarnetilsynUtil.søknad))
        every { søknadService.hentSøknad(søknad.id) } returns søknad

        pdfService.lagPdf(søknad.id)

        assertGenerertHtml("søknad/barnetilsyn.html")
        assertThat(oppdaterSøknadSlot.captured.søknadPdf).isEqualTo(pdfBytes)
    }

    private fun assertGenerertHtml(filnavn: String) {
        // Kan brukes ved endringer for å skrive ny output til fil og sen verifisere
        // skrivTilFil(filnavn, htmlSlot.captured)
        assertThat(htmlSlot.captured).isEqualTo(FileUtil.readFile(filnavn))
    }

    @Suppress("unused")
    private fun skrivTilFil(navn: String, data: String) {
        val file = File("src/test/resources/$navn")
        if (!file.exists()) {
            file.createNewFile()
        }
        file.writeText(data)
    }

    private fun lagSøknad(stønadstype: Stønadstype, data: Any) = Søknad(
        søknadJson = JsonWrapper(objectMapper.writeValueAsString(data)),
        type = stønadstype,
        personIdent = "1",
        opprettetTid = LocalDateTime.now(),
    )
}
