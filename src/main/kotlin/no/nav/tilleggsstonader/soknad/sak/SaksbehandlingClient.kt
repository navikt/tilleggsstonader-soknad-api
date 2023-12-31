package no.nav.tilleggsstonader.soknad.sak

import no.nav.tilleggsstonader.kontrakter.sak.journalføring.AutomatiskJournalføringRequest
import no.nav.tilleggsstonader.libs.http.client.AbstractRestClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class SaksbehandlingClient(
    @Value("\${clients.sak.uri}") private val uri: URI,
    @Qualifier("azureClientCredential")
    restTemplate: RestTemplate,
) : AbstractRestClient(restTemplate) {

    private val sakUri = UriComponentsBuilder.fromUri(uri).pathSegment("api", "ekstern").build()

    fun sendTilSak(request: AutomatiskJournalføringRequest) {
        val uri = UriComponentsBuilder.fromUri(sakUri.toUri())
            .pathSegment("handter-soknad").build().toUriString()
        postForEntityNullable<Void>(uri, request)
    }
}
