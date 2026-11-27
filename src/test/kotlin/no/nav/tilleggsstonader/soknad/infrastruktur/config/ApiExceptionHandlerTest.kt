package no.nav.tilleggsstonader.soknad.infrastruktur.config

import org.apache.hc.client5.http.ConnectTimeoutException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import java.net.SocketTimeoutException

class ApiExceptionHandlerTest {
    private val handler = ApiExceptionHandler()

    @Test
    fun `connect-timeout mot annet system gir 503`() {
        val feil = ResourceAccessException("timeout", ConnectTimeoutException("timeout"))

        assertThat(handler.handleKallMotAnnetSystemFeilet(feil).status).isEqualTo(503)
    }

    @Test
    fun `read-timeout mot annet system gir 500`() {
        val feil = ResourceAccessException("timeout", SocketTimeoutException())

        assertThat(handler.handleKallMotAnnetSystemFeilet(feil).status).isEqualTo(500)
    }

    @Test
    fun `502, 503 og 504 fra annet system gir 503`() {
        listOf(HttpStatus.BAD_GATEWAY, HttpStatus.SERVICE_UNAVAILABLE, HttpStatus.GATEWAY_TIMEOUT).forEach {
            val feil = HttpServerErrorException.create(it, "feil", HttpHeaders.EMPTY, ByteArray(0), null)

            assertThat(handler.handleKallMotAnnetSystemFeilet(feil).status).isEqualTo(503)
        }
    }

    @Test
    fun `500 fra annet system gir 500`() {
        val feil = HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "feil", HttpHeaders.EMPTY, ByteArray(0), null)

        assertThat(handler.handleKallMotAnnetSystemFeilet(feil).status).isEqualTo(500)
    }
}
