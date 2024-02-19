package no.nav.tilleggsstonader.soknad.dokument

import no.nav.tilleggsstonader.libs.http.client.AbstractRestClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.util.UUID

@Component
class FamilieVedleggClient(
    @Value("\${clients.familie-dokument.uri}")
    private val dokumentApiURI: URI,
    @Qualifier("tokenExchange") restTemplate: RestTemplate,
) :
    AbstractRestClient(restTemplate) {

    private val hentVedleggUri = UriComponentsBuilder.fromUri(dokumentApiURI)
        .path(HENT)
        .pathSegment("{id}")
        .encode().toUriString()

    fun hentVedlegg(vedleggId: UUID): ByteArray {
        return getForEntity(hentVedleggUri, HENT_HEADERS, mapOf("id" to vedleggId))
    }

    companion object {

        private const val HENT = "api/mapper/tilleggsstonad"
        private val HENT_HEADERS = HttpHeaders().apply {
            accept = listOf(MediaType.APPLICATION_OCTET_STREAM)
        }
    }
}
