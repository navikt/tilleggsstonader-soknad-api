package no.nav.tilleggsstonader.soknad.infrastruktur

import no.nav.security.token.support.core.api.Unprotected
import no.nav.tilleggsstonader.soknad.IntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchException
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpServerErrorException.InternalServerError
import org.springframework.web.client.exchange
import org.springframework.web.client.getForEntity
import org.springframework.web.client.postForEntity
import java.time.LocalDate
import java.time.LocalDateTime

class TestControllerTest : IntegrationTest() {

    val json = """{"tekst":"abc","dato":"2023-01-01","tidspunkt":"2023-01-01T12:00:03"}"""
    val feilJson =
        """{"type":"about:blank","title":"Internal Server Error","status":500,"detail":"Ukjent feil","instance":"/api/test/error"}"""

    @Test
    fun `skal kunne hente json fra endepunkt`() {
        val response = restTemplate.getForEntity<String>(localhost("api/test"))
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body!!).isEqualTo(json)
    }

    @Test
    fun `skal kunne sende inn object`() {
        val json = TestObject(
            tekst = "abc",
            dato = LocalDate.of(2023, 1, 1),
            tidspunkt = LocalDateTime.of(2023, 1, 1, 12, 0, 3),
        )

        val response = restTemplate.postForEntity<TestObject>(localhost("api/test"), json)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body!!).isEqualTo(json)
    }

    @Test
    fun `skal kunne sende inn object med json header`() {
        val json = TestObject(
            tekst = "abc",
            dato = LocalDate.of(2023, 1, 1),
            tidspunkt = LocalDateTime.of(2023, 1, 1, 12, 0, 3),
        )
        val jsonHeaders = HttpHeaders().apply {
            contentType = APPLICATION_JSON
            accept = listOf(APPLICATION_JSON)
        }

        val response = restTemplate.postForEntity<TestObject>(localhost("api/test"), HttpEntity(json, jsonHeaders))
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body!!).isEqualTo(json)
    }

    @Test
    fun `skal håndtere ukjent feil`() {
        var response = catchException { restTemplate.getForEntity<String>(localhost("api/test/error")) }
        assertThat(response).isInstanceOf(InternalServerError::class.java)
        assertInternalServerError(response as InternalServerError)
    }

    @Test
    fun `skal håndtere ukjent feil med forventet responstype`() {
        val response = catchException { restTemplate.getForEntity<TestObject>(localhost("api/test/error")) }
        assertThat(response).isInstanceOf(InternalServerError::class.java)
        assertInternalServerError(response as InternalServerError)
    }

    @Test
    fun `skal håndtere ukjent feil med forventet responstype med exchange`() {
        val response = catchException { restTemplate.exchange<TestObject>(localhost("api/test/error"), HttpMethod.GET) }
        assertThat(response).isInstanceOf(InternalServerError::class.java)
        assertInternalServerError(response as InternalServerError)
    }

    private fun assertInternalServerError(response: InternalServerError) {
        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(response.responseHeaders?.contentType).isEqualTo(APPLICATION_PROBLEM_JSON)
        assertThat(response.responseBodyAsString).isEqualTo(feilJson)
    }
}

@RestController
@RequestMapping("/api/test")
@Unprotected
class TestController {

    @GetMapping
    fun get(): TestObject {
        return TestObject(
            tekst = "abc",
            dato = LocalDate.of(2023, 1, 1),
            tidspunkt = LocalDateTime.of(2023, 1, 1, 12, 0, 3),
        )
    }

    @PostMapping
    fun post(@RequestBody testObject: TestObject): TestObject {
        return testObject
    }

    @GetMapping("error")
    fun error() {
        error("error")
    }
}

data class TestObject(
    val tekst: String,
    val dato: LocalDate,
    val tidspunkt: LocalDateTime,
)
