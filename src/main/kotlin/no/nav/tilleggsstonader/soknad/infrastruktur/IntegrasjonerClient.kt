package no.nav.tilleggsstonader.soknad.infrastruktur

import no.nav.tilleggsstonader.kontrakter.dokarkiv.ArkiverDokumentRequest
import no.nav.tilleggsstonader.kontrakter.dokarkiv.ArkiverDokumentResponse
import no.nav.tilleggsstonader.libs.http.client.AbstractRestClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class IntegrasjonerClient (
    @Value("\${clients.integrasjoner.uri}") private val uri: URI,
    @Qualifier("azureClientCredential")
    restTemplate: RestTemplate
): AbstractRestClient(restTemplate) {

    private val sendInnUri = UriComponentsBuilder.fromUri(uri).pathSegment("arkiv").toUriString()

    fun arkiver(arkiverDokumentRequest: ArkiverDokumentRequest): ArkiverDokumentResponse {
        return postForEntity<ArkiverDokumentResponse>(sendInnUri, arkiverDokumentRequest)
    }

}