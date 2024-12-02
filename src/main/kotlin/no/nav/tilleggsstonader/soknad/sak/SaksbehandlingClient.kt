package no.nav.tilleggsstonader.soknad.sak

import no.nav.tilleggsstonader.kontrakter.felles.IdentStønadstype
import no.nav.tilleggsstonader.kontrakter.sak.journalføring.HåndterSøknadRequest
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

    fun sendTilSak(request: HåndterSøknadRequest) {
        val uri = UriComponentsBuilder.fromUri(sakUri.toUri())
            .pathSegment("handter-soknad").build().toUriString()
        postForEntityNullable<Void>(uri, request)
    }

    fun skalRoutesTilNyLøsning(request: IdentStønadstype): Boolean {
        val uri = UriComponentsBuilder.fromUri(sakUri.toUri())
            .pathSegment("routing-soknad").build().toUriString()
        return postForEntity<SkalRoutesINyLøsning>(uri, request).skalBehandlesINyLøsning
    }
    fun hentBehandlingStatus(request: IdentStønadstype): Boolean {
        val uri = UriComponentsBuilder.fromUri(sakUri.toUri())
            .pathSegment("harBehandling").build().toUriString()
        return postForEntity<Boolean>(uri, request)
    }
}

private data class SkalRoutesINyLøsning(
    val skalBehandlesINyLøsning: Boolean,
)
