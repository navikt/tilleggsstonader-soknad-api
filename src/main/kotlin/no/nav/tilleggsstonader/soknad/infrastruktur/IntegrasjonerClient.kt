package no.nav.tilleggsstonader.soknad.infrastruktur

import no.nav.tilleggsstonader.kontrakter.dokarkiv.ArkiverDokumentRequest
import no.nav.tilleggsstonader.kontrakter.dokarkiv.ArkiverDokumentResponse
import no.nav.tilleggsstonader.kontrakter.felles.ObjectMapperProvider.jsonMapper
import no.nav.tilleggsstonader.libs.http.client.AbstractRestClient
import no.nav.tilleggsstonader.libs.log.SecureLogger.secureLogger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import tools.jackson.module.kotlin.readValue
import java.net.URI

@Service
class IntegrasjonerClient(
    @Value("\${clients.integrasjoner.uri}") private val uri: URI,
    @Qualifier("azureClientCredential")
    restTemplate: RestTemplate,
) : AbstractRestClient(restTemplate) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val sendInnUri = UriComponentsBuilder.fromUri(uri).pathSegment("api", "arkiv").toUriString()

    fun arkiver(request: ArkiverDokumentRequest): ArkiverDokumentResponse {
        try {
            return postForEntity<ArkiverDokumentResponse>(sendInnUri, request)
        } catch (e: HttpClientErrorException.Conflict) {
            if (e.responseBodyAsString.contains("journalpostId")) {
                try {
                    logger.warn("409 conflict for eksternReferanseId=${request.eksternReferanseId} ved journalføring")
                    return jsonMapper.readValue<ArkiverDokumentResponse>(e.responseBodyAsString)
                } catch (ex: Exception) {
                    secureLogger.error("Feilet parsing av 409 response=${e.responseBodyAsString}")
                    throw e
                }
            }
            throw e
        }
    }
}
