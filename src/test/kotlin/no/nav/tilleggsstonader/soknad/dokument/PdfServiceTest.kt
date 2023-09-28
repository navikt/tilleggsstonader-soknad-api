package no.nav.tilleggsstonader.soknad.dokument

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import no.nav.tilleggsstonader.soknad.dokument.pdf.HtmlGenerator
import no.nav.tilleggsstonader.soknad.soknad.SøknadService
import no.nav.tilleggsstonader.soknad.soknad.SøknadTestUtil.lagSøknad
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.SøknadBarnetilsynUtil
import no.nav.tilleggsstonader.soknad.soknad.domene.Søknad
import no.nav.tilleggsstonader.soknad.util.FileUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

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
        val søknad = lagSøknad(SøknadBarnetilsynUtil.søknad)
        every { søknadService.hentSøknad(søknad.id) } returns søknad

        pdfService.lagPdf(søknad.id)

        assertGenerertHtml("søknad/barnetilsyn.html")
        assertThat(oppdaterSøknadSlot.captured.søknadPdf).isEqualTo(pdfBytes)
    }

    private fun assertGenerertHtml(filnavn: String) {
        // Kan brukes ved endringer for å skrive ny output til fil og sen verifisere
        // FileUtil.skrivTilFil(filnavn, htmlSlot.captured)
        // kan brukes for å generere en pdf å verifisere at den ser riktig ut
        // generatePdf(htmlSlot.captured, "$filnavn.pdf")

        assertThat(htmlSlot.captured).isEqualTo(FileUtil.readFile(filnavn))
    }

    @Suppress("unused")
    private fun generatePdf(html: String, name: String) {
        val url = "https://familie-dokument.intern.dev.nav.no/api/html-til-pdf"
        val request = HttpEntity(html, HttpHeaders().apply {
            accept = listOf(MediaType.APPLICATION_PDF)
        })
        val pdf = TestRestTemplate().postForEntity<ByteArray>(url, request).body!!
        FileUtil.skrivTilFil(name, pdf)
    }
}
