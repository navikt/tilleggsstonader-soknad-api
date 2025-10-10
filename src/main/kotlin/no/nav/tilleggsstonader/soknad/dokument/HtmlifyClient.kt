package no.nav.tilleggsstonader.soknad.dokument

import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.kontrakter.sak.DokumentBrevkode
import no.nav.tilleggsstonader.libs.http.client.AbstractRestClient
import no.nav.tilleggsstonader.soknad.dokument.pdf.HtmlFelt
import no.nav.tilleggsstonader.soknad.dokument.pdf.Søkerinformasjon
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.time.LocalDateTime

@Component
class HtmlifyClient(
    @Value("\${clients.htmlify.uri}")
    private val uri: URI,
    @Qualifier("utenAuth") restTemplate: RestTemplate,
) : AbstractRestClient(restTemplate) {
    fun genererSøknadHtml(
        stønadstype: Stønadstype,
        tittel: String,
        felter: List<HtmlFelt>,
        mottattTidspunkt: LocalDateTime,
        dokumentasjon: List<DokumentasjonAvsnitt>,
        dokumentBrevkode: DokumentBrevkode,
    ): String =
        postForEntity<String>(
            UriComponentsBuilder.fromUri(uri).pathSegment("api", "soknad").toUriString(),
            mapOf(
                "type" to stønadstype,
                "tittel" to tittel,
                "skjemanummer" to dokumentBrevkode.verdi,
                "mottattTidspunkt" to mottattTidspunkt,
                "felter" to felter,
                "dokumentasjon" to dokumentasjon,
            ),
        )

    fun genererKjørelisteHtml(kjørelisteHtmlRequest: KjørelisteHtmlRequest): String =
        postForEntity<String>(
            UriComponentsBuilder.fromUri(uri).pathSegment("api", "kjoreliste").toUriString(),
            kjørelisteHtmlRequest,
        )
}

data class DokumentasjonAvsnitt(
    val label: String,
    val dokument: List<Dokument>,
)

data class Dokument(
    val label: String,
    val labelAntall: String,
)

data class KjørelisteHtmlRequest(
    val tittel: String,
    val skjemanummer: String,
    val mottattTidspunkt: LocalDateTime,
    val uker: List<KjørelisteUkeHtmlRequest>,
    val dokumentasjon: List<DokumentasjonAvsnitt>,
    val søker: Søkerinformasjon,
)

data class KjørelisteUkeHtmlRequest(
    val ukeTekst: String,
    val spørsmål: String,
    val dager: List<KjørelisteDagHtmlRequest>,
)

data class KjørelisteDagHtmlRequest(
    val datoTekst: String,
    val harKjørt: Boolean,
    val parkeringsutgift: KjørelisteParkeringsutgiftHtmlRequest?,
)

data class KjørelisteParkeringsutgiftHtmlRequest(
    val tekst: String,
    val beløp: Number,
)
