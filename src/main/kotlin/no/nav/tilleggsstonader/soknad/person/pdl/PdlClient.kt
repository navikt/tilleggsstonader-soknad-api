package no.nav.tilleggsstonader.soknad.person.pdl

import no.nav.tilleggsstonader.libs.utils.fnr.Fødselsnummer
import no.nav.tilleggsstonader.soknad.person.pdl.PdlUtil.httpHeaders
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlPersonRequest
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlPersonRequestVariables
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlResponse
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlSøker
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlSøkerData
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import org.springframework.web.client.exchange
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class PdlClient(
    @Value("\${clients.pdl.uri}")
    private val pdlUrl: URI,
    @Qualifier("tokenExchange")
    private val restOperations: RestOperations,
) {

    fun hentSøker(fødselsnummer: Fødselsnummer): PdlSøker {
        val pdlPersonRequest = PdlPersonRequest(
            variables = PdlPersonRequestVariables(fødselsnummer.verdi),
            query = PdlUtil.søkerQuery,
        )
        val pdlResponse = restOperations.exchange<PdlResponse<PdlSøkerData>>(
            graphqlUri,
            HttpMethod.POST,
            HttpEntity(pdlPersonRequest, httpHeaders),
        ).body ?: error("Mangler body")
        return feilsjekkOgReturnerData(fødselsnummer.verdi, pdlResponse) { it.person }
    }

    private val graphqlUri = UriComponentsBuilder.fromUri(pdlUrl)
        .pathSegment("graphql")
        .build().toUri()
}
