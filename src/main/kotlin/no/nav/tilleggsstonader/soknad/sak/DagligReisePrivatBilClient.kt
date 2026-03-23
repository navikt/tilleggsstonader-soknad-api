package no.nav.tilleggsstonader.soknad.sak

import no.nav.tilleggsstonader.kontrakter.søknad.RammevedtakDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.body
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class DagligReisePrivatBilClient(
    @Value("\${clients.sak.uri}") private val uri: URI,
    @Qualifier("restClientTokenExchange") private val restClient: RestClient,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val sakUri = UriComponentsBuilder.fromUri(uri).pathSegment("api", "ekstern", "privat-bil").build()

    fun hentRammevedtakForInnloggetBruker(): List<RammevedtakDto> {
        val uri =
            UriComponentsBuilder
                .fromUri(sakUri.toUri())
                .pathSegment("rammevedtak")
                .build()
                .toUriString()

        return try {
            restClient
                .get()
                .uri(uri)
                .retrieve()
                .body<List<RammevedtakDto>>()
                ?: emptyList()
        } catch (e: RestClientResponseException) {
            logger.warn("Feil ved henting av rammevedtak: ${e.statusCode}")
            emptyList()
        }
    }
}
