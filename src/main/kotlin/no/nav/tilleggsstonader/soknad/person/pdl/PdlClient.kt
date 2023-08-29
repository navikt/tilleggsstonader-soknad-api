package no.nav.tilleggsstonader.soknad.person.pdl

import no.nav.tilleggsstonader.soknad.person.pdl.PdlUtil.httpHeaders
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlPersonRequest
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlPersonRequestVariables
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlResponse
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlSøker
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlSøkerData
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import org.springframework.web.client.postForEntity
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class PdlClient(
    @Value("\${clients.pdl.uri}")
    private val pdlUrl: URI,
    @Qualifier("tokenExchange")
    private val restOperations: RestOperations,
) {

    fun hentSøker(personIdent: String): PdlSøker {
        val pdlPersonRequest = PdlPersonRequest(
            variables = PdlPersonRequestVariables(personIdent),
            query = PdlUtil.søkerQuery,
        )
        val pdlResponse = restOperations.postForEntity<PdlResponse<PdlSøkerData>>(
            graphqlUri,
            HttpEntity(pdlPersonRequest, httpHeaders),
        ).body ?: error("Mangler body")
        return feilsjekkOgReturnerData(personIdent, pdlResponse) { it.person }
    }

    private val graphqlUri = UriComponentsBuilder.fromUri(pdlUrl)
        .pathSegment("graphql")
        .build().toUri()
}
