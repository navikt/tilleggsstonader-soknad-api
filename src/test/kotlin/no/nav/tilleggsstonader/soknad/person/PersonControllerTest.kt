package no.nav.tilleggsstonader.soknad.person

import io.mockk.verify
import no.nav.tilleggsstonader.libs.test.fnr.FnrGenerator
import no.nav.tilleggsstonader.libs.utils.fnr.Fødselsnummer
import no.nav.tilleggsstonader.soknad.IntegrationTest
import no.nav.tilleggsstonader.soknad.person.dto.PersonMedBarnDto
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.web.client.exchange
import org.springframework.web.client.getForEntity
import java.time.LocalDate

class PersonControllerTest : IntegrationTest() {

    @Test
    fun `skal feile hvis man ikke har med token`() {
        assertThatThrownBy {
            restTemplate.getForEntity<PersonMedBarnDto>(localhost("api/person"))
        }.hasMessage("""401 : "{"type":"about:blank","title":"Unauthorized","status":401,"detail":"Ukjent feil","instance":"/api/person"}"""")
    }

    @Test
    fun `skal kunne hente person fra pdl`() {
        val url = localhost("api/person")
        val personident = FnrGenerator.generer(LocalDate.of(2000, 1, 1))
        headers.setBearerAuth(søkerBearerToken(personident))

        val response = restTemplate.exchange<PersonMedBarnDto>(url, HttpMethod.GET, HttpEntity(null, headers))

        verify(exactly = 1) { pdlClient.hentSøker(Fødselsnummer(personident)) }
        assertThat(response.body!!.navn).isEqualTo("fornavn etternavn")
    }
}
