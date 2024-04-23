package no.nav.tilleggsstonader.soknad.person.pdl

import no.nav.tilleggsstonader.soknad.person.pdl.PdlUtil.httpHeaders
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlBarn
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlBolkResponse
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlPersonBolkRequest
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlPersonBolkRequestVariables
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlPersonRequest
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlPersonRequestVariables
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlResponse
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlSøkerNavn
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlSøkerNavnData
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
class PdlClientCredentialClient(
    @Value("\${clients.pdl.uri}")
    private val pdlUrl: URI,
    @Qualifier("azureClientCredential")
    private val restOperations: RestOperations,
) {

    fun hentNavn(ident: String): PdlSøkerNavn {
        val pdlPersonRequest = PdlPersonRequest(
            variables = PdlPersonRequestVariables(ident),
            query = PdlUtil.søkerQuery,
        )
        val pdlResponse = restOperations.exchange<PdlResponse<PdlSøkerNavnData>>(
            graphqlUri,
            HttpMethod.POST,
            HttpEntity(pdlPersonRequest, httpHeaders),
        ).body ?: error("Mangler body")
        return feilsjekkOgReturnerData(ident, pdlResponse) { it.person }
    }

    fun hentBarn(personIdenter: List<String>): Map<String, PdlBarn> {
        if (personIdenter.isEmpty()) return emptyMap()
        val pdlPersonRequest = PdlPersonBolkRequest(
            variables = PdlPersonBolkRequestVariables(personIdenter),
            query = PdlUtil.barnQuery,
        )
        val pdlResponse = restOperations.exchange<PdlBolkResponse<PdlBarn>>(
            graphqlUri,
            HttpMethod.POST,
            HttpEntity(pdlPersonRequest, httpHeaders),
        ).body ?: error("Mangler body")
        return feilsjekkOgReturnerData(pdlResponse)
    }

    private val graphqlUri = UriComponentsBuilder.fromUri(pdlUrl)
        .pathSegment("graphql")
        .build().toUri()
}
