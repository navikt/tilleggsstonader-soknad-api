package no.nav.tilleggsstonader.soknad.sak

import no.nav.tilleggsstonader.kontrakter.felles.IdentStønadstype
import no.nav.tilleggsstonader.libs.http.client.AbstractRestClient
import no.nav.tilleggsstonader.soknad.kjøreliste.RammevedtakDto
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class DagligReisePrivatBilClient(
    @Value("\${clients.sak.uri}") private val uri: URI,
    @Qualifier("azureClientCredential")
    restTemplate: RestTemplate,
) : AbstractRestClient(restTemplate) {
    private val sakUri = UriComponentsBuilder.fromUri(uri).pathSegment("api", "ekstern", "privat-bil").build()

    fun hentRammevedtak(request: IdentStønadstype): List<RammevedtakDto> {
        val uri =
            UriComponentsBuilder
                .fromUri(sakUri.toUri())
                .pathSegment("rammevedtak")
                .build()
                .toUriString()
        return postForEntity<List<RammevedtakDto>>(uri, request)
    }
}
