package no.nav.tilleggsstonader.soknad.dokument

import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.libs.http.client.AbstractRestClient
import no.nav.tilleggsstonader.soknad.dokument.pdf.Avsnitt
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

    fun generateHtml(
        stønadstype: Stønadstype,
        avsnitt: Avsnitt,
        mottattTidspunkt: LocalDateTime,
        dokumentasjon: List<DokumentasjonAvsnitt>,
    ): String {
        return postForEntity<String>(
            UriComponentsBuilder.fromUri(uri).pathSegment("api", "soknad").toUriString(),
            mapOf(
                "type" to stønadstype,
                "mottattTidspunkt" to mottattTidspunkt,
                "avsnitt" to avsnitt,
                "dokumentasjon" to dokumentasjon,
            ),
        )
    }
}

data class DokumentasjonAvsnitt(
    val label: String,
    val dokument: List<Dokument>,
)

data class Dokument(
    val label: String,
    val labelSendtInnTidligere: String,
    val labelAntall: String,
)
