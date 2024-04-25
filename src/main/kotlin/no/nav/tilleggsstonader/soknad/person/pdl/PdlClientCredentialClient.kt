package no.nav.tilleggsstonader.soknad.person.pdl

import no.nav.tilleggsstonader.libs.http.client.AbstractRestClient
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
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class PdlClientCredentialClient(
    @Value("\${clients.pdl.uri}")
    private val pdlUrl: URI,
    @Qualifier("azureClientCredential")
    restTemplate: RestTemplate,
) : AbstractRestClient(restTemplate) {

    fun hentNavn(ident: String): PdlSøkerNavn {
        val pdlPersonRequest = PdlPersonRequest(
            variables = PdlPersonRequestVariables(ident),
            query = PdlUtil.søkerQuery,
        )
        val pdlResponse = postForEntity<PdlResponse<PdlSøkerNavnData>>(graphqlUri, pdlPersonRequest, httpHeaders)
        return feilsjekkOgReturnerData(ident, pdlResponse) { it.person }
    }

    fun hentBarn(personIdenter: List<String>): Map<String, PdlBarn> {
        if (personIdenter.isEmpty()) return emptyMap()
        val pdlPersonRequest = PdlPersonBolkRequest(
            variables = PdlPersonBolkRequestVariables(personIdenter),
            query = PdlUtil.barnQuery,
        )
        val pdlResponse = postForEntity<PdlBolkResponse<PdlBarn>>(graphqlUri, pdlPersonRequest, httpHeaders)
        return feilsjekkOgReturnerData(pdlResponse)
    }

    private val graphqlUri = UriComponentsBuilder.fromUri(pdlUrl)
        .pathSegment("graphql")
        .toUriString()
}
