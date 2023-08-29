package no.nav.tilleggsstonader.soknad.soknad

import no.nav.tilleggsstonader.soknad.IntegrationTest
import no.nav.tilleggsstonader.soknad.soknad.dto.Kvittering
import no.nav.tilleggsstonader.soknad.tokenSubject
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.web.client.postForEntity
import java.time.LocalDate

class SøknadControllerTest : IntegrationTest() {

    val request = HttpEntity(mapOf<String, Any>(), headers)

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(søkerBearerToken(tokenSubject))
    }

    @Test
    fun `skal kunne sende inn en søknad for barnetilsyn`() {
        val response = restTemplate.postForEntity<Kvittering>(localhost("api/soknad/barnetilsyn"), request)
        assertThat(response.body!!.mottattTidspunkt.toLocalDate()).isEqualTo(LocalDate.now())
    }

    @Test
    fun `skal feile når requesten mangler token`() {
        headers.remove(HttpHeaders.AUTHORIZATION)
        assertThatThrownBy {
            restTemplate.postForEntity<Kvittering>(localhost("api/soknad/barnetilsyn"), request)
        }.hasMessage("""401 : "{"type":"about:blank","title":"Unauthorized","status":401,"detail":"Ukjent feil","instance":"/api/soknad/barnetilsyn"}"""")
    }
}
