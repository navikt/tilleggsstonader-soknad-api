package no.nav.tilleggsstonader.soknad.dokument

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import no.nav.tilleggsstonader.kontrakter.felles.ObjectMapperProvider.objectMapper
import no.nav.tilleggsstonader.soknad.person.PersonService
import no.nav.tilleggsstonader.soknad.soknad.SøknadService
import no.nav.tilleggsstonader.soknad.soknad.SøknadTestUtil.lagSøknad
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.SøknadBarnetilsynUtil
import no.nav.tilleggsstonader.soknad.soknad.domene.Skjema
import no.nav.tilleggsstonader.soknad.soknad.læremidler.SøknadLæremidlerUtil
import no.nav.tilleggsstonader.soknad.util.FileUtil
import no.nav.tilleggsstonader.soknad.util.FileUtil.listFiles
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import java.net.URI

class PdfServiceTest {
    private val søknadService = mockk<SøknadService>()
    private val familieDokumentClient = mockk<FamilieDokumentClient>()
    private val htmlifyClient = lagHtmlifyClient()
    private val personService = mockk<PersonService>()

    private val pdfService = PdfService(søknadService, personService, htmlifyClient, familieDokumentClient)

    val oppdaterSkjemaSlot = slot<Skjema>()

    val htmlSlot = slot<String>()
    private val pdfBytes = "pdf".toByteArray()

    @BeforeEach
    fun setUp() {
        justRun { søknadService.oppdaterSøknad(capture(oppdaterSkjemaSlot)) }
        every { personService.hentNavnMedClientCredential(any()) } returns "Fornavn etternavn"
        every { familieDokumentClient.genererPdf(capture(htmlSlot)) } returns pdfBytes
    }

    @Disabled // html-fila må prettyfies etter at den har blitt generert, htmlify returnerer all html som en rad
    @Nested
    inner class GenereringAvPdf {
        @Test
        fun `skal lage pdf fra barnetilsyn`() {
            val søknad = lagSøknad(SøknadBarnetilsynUtil.søknad)
            every { søknadService.hentSøknad(søknad.id) } returns søknad

            pdfService.lagPdf(søknad.id)

            assertGenerertHtml("søknad/barnetilsyn/barnetilsyn.html")
            assertThat(oppdaterSkjemaSlot.captured.søknadPdf).isEqualTo(pdfBytes)
        }

        @Test
        fun `skal lage pdf fra læremidler`() {
            val søknad = lagSøknad(SøknadLæremidlerUtil.søknad)
            every { søknadService.hentSøknad(søknad.id) } returns søknad

            pdfService.lagPdf(søknad.id)

            assertGenerertHtml("søknad/læremidler/læremidler.html")
            assertThat(oppdaterSkjemaSlot.captured.søknadPdf).isEqualTo(pdfBytes)
        }
    }

    @Test
    fun `html skal være formattert for å enklere kunne sjekke diff`() {
        val htmlFiler =
            listFiles("søknad")
                .flatMap { it.walk() }
                .filter { it.name.endsWith(".html") }
                .map { it.toString().let { it.substring(it.lastIndexOf("søknad/")) } }

        assertThat(htmlFiler).isNotEmpty
        htmlFiler.forEach { fil ->
            val erIkkeFormatert =
                FileUtil
                    .readFile(fil)
                    .split("\n")
                    .none { it.contains("<body") && it.contains("<div") }
            assertThat(erIkkeFormatert).isTrue()
        }
    }

    private fun assertGenerertHtml(filnavn: String) {
        // Kan brukes ved endringer for å skrive ny output til fil og sen verifisere
        FileUtil.skrivTilFil(filnavn, htmlSlot.captured)
        // kan brukes for å generere en pdf å verifisere at den ser riktig ut
        generatePdf(htmlSlot.captured, "$filnavn.pdf")

        assertThat(htmlSlot.captured).isEqualTo(FileUtil.readFile(filnavn))
    }

    private fun lagHtmlifyClient(): HtmlifyClient {
        val restTemplate = TestRestTemplate().restTemplate
        restTemplate.messageConverters.removeIf { it is MappingJackson2HttpMessageConverter }
        restTemplate.messageConverters.add(MappingJackson2HttpMessageConverter(objectMapper))
        val url = "https://tilleggsstonader-htmlify.intern.dev.nav.no"
        // val url = "http://localhost:8001"
        return HtmlifyClient(URI.create(url), restTemplate)
    }

    @Suppress("unused")
    private fun generatePdf(
        html: String,
        name: String,
    ) {
        val url = "https://familie-dokument.intern.dev.nav.no/api/html-til-pdf"
        val request =
            HttpEntity(
                html,
                HttpHeaders().apply {
                    accept = listOf(MediaType.APPLICATION_PDF)
                },
            )
        val pdf = TestRestTemplate().postForEntity<ByteArray>(url, request).body!!
        FileUtil.skrivTilFil(name, pdf)
    }
}
