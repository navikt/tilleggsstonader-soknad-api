package no.nav.tilleggsstonader.soknad.sak

import no.nav.tilleggsstonader.kontrakter.felles.IdentSkjematype
import no.nav.tilleggsstonader.kontrakter.felles.IdentStønadstype
import no.nav.tilleggsstonader.kontrakter.søknad.felles.SkjemaRoutingResponse
import no.nav.tilleggsstonader.libs.http.client.postForEntity
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
    private val restTemplate: RestTemplate,
) {
    private val sakUri = UriComponentsBuilder.fromUri(uri).pathSegment("api", "ekstern").build()

    fun finnSkjemaRoutingAksjon(request: IdentSkjematype): SkjemaRoutingResponse {
        val uri =
            UriComponentsBuilder
                .fromUri(sakUri.toUri())
                .pathSegment("skjema-routing")
                .build()
                .toUriString()
        return restTemplate.postForEntity<SkjemaRoutingResponse>(uri, request)
    }

    fun harBehandlingUnderArbeid(request: IdentStønadstype): Boolean {
        val uri =
            UriComponentsBuilder
                .fromUri(sakUri.toUri())
                .pathSegment("har-behandling")
                .build()
                .toUriString()
        return restTemplate.postForEntity<Boolean>(uri, request)
    }
}
