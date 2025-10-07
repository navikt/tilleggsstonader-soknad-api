package no.nav.tilleggsstonader.soknad.infrastruktur

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.Unprotected
import no.nav.tilleggsstonader.libs.sikkerhet.EksternBrukerUtils
import no.nav.tilleggsstonader.libs.test.assertions.catchThrowableOfType
import no.nav.tilleggsstonader.libs.test.httpclient.ProblemDetailUtil.catchProblemDetailException
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
import org.springframework.http.ProblemDetail
import org.springframework.http.converter.HttpMessageConversionException
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException.InternalServerError
import org.springframework.web.client.exchange
import org.springframework.web.client.getForEntity
import org.springframework.web.client.postForEntity
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.collections.addAll
import kotlin.text.get

class TestControllerTest : IntegrationTest() {
    val json = """{"tekst":"abc","dato":"2023-01-01","tidspunkt":"2023-01-01T12:00:03"}"""
    val feilJson =
        """{"type":"about:blank","title":"Internal Server Error","status":500,"detail":"Ukjent feil","instance":"/api/test/error"}"""

    @Test
    fun `skal kunne hente json fra endepunkt`() {
        webTestClient
            .get()
            .uri("/api/test")
            .medSøkerBearerToken()
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .json(json)
    }

    @Test
    fun `skal kunne sende inn object`() {
        val json =
            TestObject(
                tekst = "abc",
                dato = LocalDate.of(2023, 1, 1),
                tidspunkt = LocalDateTime.of(2023, 1, 1, 12, 0, 3),
            )

        webTestClient
            .post()
            .uri("/api/test")
            .bodyValue(json)
            .medSøkerBearerToken()
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<TestObject>()
            .isEqualTo(json)
    }

    @Test
    fun `skal kunne sende inn object med json header`() {
        val json =
            TestObject(
                tekst = "abc",
                dato = LocalDate.of(2023, 1, 1),
                tidspunkt = LocalDateTime.of(2023, 1, 1, 12, 0, 3),
            )
        val jsonHeaders =
            HttpHeaders().apply {
                contentType = APPLICATION_JSON
                accept = listOf(APPLICATION_JSON)
            }

        webTestClient
            .post()
            .uri("/api/test")
            .headers { it.addAll(jsonHeaders) }
            .bodyValue(json)
            .medSøkerBearerToken()
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<TestObject>()
            .isEqualTo(json)
    }

    @Test
    fun `skal håndtere ukjent feil`() {
        webTestClient
            .get()
            .uri("/api/test/error")
            .medSøkerBearerToken()
            .exchange()
            .expectStatus()
            .is5xxServerError
    }

    @Test
    fun `skal håndtere ukjent feil med forventet responstype`() {
        webTestClient
            .get()
            .uri("/api/test/error")
            .medSøkerBearerToken()
            .exchange()
            .expectStatus()
            .is5xxServerError
    }

    @Test
    fun `skal håndtere kall mot endepunkt som ikke eksisterer`() {
        webTestClient
            .get()
            .uri("/api/eksistererIkke")
            .medSøkerBearerToken()
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `skal håndtere kall mot protected endepunkt uten token`() {
        webTestClient
            .get()
            .uri("/api/test/protected-feil")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `skal feile hvis påkrevd booleanfelt er null`() {
        webTestClient
            .post()
            .uri("/api/test/boolean")
            .bodyValue(mapOf<Any, Any>())
            .medSøkerBearerToken()
            .exchange()
            .expectStatus()
            .is5xxServerError
            .expectBody()
            .jsonPath("$.detail")
            .value<String> {
                assertThat(it).contains("Missing required creator property 'verdi'")
            }
    }
}

@RestController
@RequestMapping("/api/test")
@Unprotected
class TestController {
    @GetMapping
    fun get(): TestObject =
        TestObject(
            tekst = "abc",
            dato = LocalDate.of(2023, 1, 1),
            tidspunkt = LocalDateTime.of(2023, 1, 1, 12, 0, 3),
        )

    @PostMapping
    fun post(
        @RequestBody testObject: TestObject,
    ): TestObject = testObject

    @GetMapping("error")
    fun error() {
        error("error")
    }

    @ProtectedWithClaims(issuer = EksternBrukerUtils.ISSUER_TOKENX, claimMap = ["acr=Level4"])
    @GetMapping("protected")
    fun protected(): Map<String, String> = mapOf()

    @ProtectedWithClaims(issuer = EksternBrukerUtils.ISSUER_TOKENX, claimMap = ["acr=Level4"])
    @GetMapping("protected-feil")
    fun protectedError() {
        error("error")
    }
}

@RestController
@RequestMapping("/api/test")
@Unprotected
class TestBooleanController {
    @ExceptionHandler(HttpMessageConversionException::class)
    fun handleThrowable(throwable: HttpMessageConversionException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, throwable.message)

    @PostMapping("/boolean")
    fun boolean(
        @RequestBody testObject: TestObjectBoolean,
    ): TestObjectBoolean = testObject
}

data class TestObject(
    val tekst: String,
    val dato: LocalDate,
    val tidspunkt: LocalDateTime,
)

data class TestObjectBoolean(
    val verdi: Boolean,
)
