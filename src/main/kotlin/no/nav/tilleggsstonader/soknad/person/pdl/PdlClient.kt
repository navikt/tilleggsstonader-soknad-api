package no.nav.tilleggsstonader.soknad.person.pdl

import no.nav.tilleggsstonader.libs.http.client.AbstractRestClient
import no.nav.tilleggsstonader.libs.utils.fnr.Fødselsnummer
import no.nav.tilleggsstonader.soknad.person.pdl.PdlUtil.httpHeaders
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlPersonRequest
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlPersonRequestVariables
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlResponse
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlSøker
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlSøkerData
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class PdlClient(
    @Value("\${clients.pdl.uri}")
    private val pdlUrl: URI,
    @Qualifier("tokenExchange")
    restTemplate: RestTemplate,
) : AbstractRestClient(restTemplate) {
    fun hentSøker(fødselsnummer: Fødselsnummer): PdlSøker {
        val pdlPersonRequest =
            PdlPersonRequest(
                variables = PdlPersonRequestVariables(fødselsnummer.verdi),
                query = PdlUtil.søkerQuery,
            )
        val pdlResponse = postForEntity<PdlResponse<PdlSøkerData>>(graphqlUri, pdlPersonRequest, httpHeaders)
        return feilsjekkOgReturnerData(fødselsnummer.verdi, pdlResponse) { it.person }
    }

    private val graphqlUri =
        UriComponentsBuilder
            .fromUri(pdlUrl)
            .pathSegment("graphql")
            .toUriString()
}
