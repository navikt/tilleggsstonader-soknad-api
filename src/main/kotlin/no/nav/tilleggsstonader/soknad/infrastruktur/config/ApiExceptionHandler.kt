package no.nav.tilleggsstonader.soknad.infrastruktur.config

import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import no.nav.tilleggsstonader.libs.log.SecureLogger.secureLogger
import no.nav.tilleggsstonader.soknad.infrastruktur.exception.GradertBrukerException
import no.nav.tilleggsstonader.soknad.soknad.SøknadValideringException
import org.apache.hc.client5.http.ConnectTimeoutException
import org.springframework.core.NestedExceptionUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class ApiExceptionHandler : ResponseEntityExceptionHandler() {
    private fun rootCause(throwable: Throwable): String = NestedExceptionUtils.getMostSpecificCause(throwable).javaClass.simpleName

    override fun handleExceptionInternal(
        ex: Exception,
        body: Any?,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        secureLogger.error("En feil har oppstått", ex)
        logger.error("En feil har oppstått - throwable=${rootCause(ex)} status=${status.value()}")
        return super.handleExceptionInternal(ex, body, headers, status, request)
    }

    @ExceptionHandler(Throwable::class)
    fun handleThrowable(throwable: Throwable): ProblemDetail {
        val responseStatus =
            throwable::class
                .annotations
                .find { it is ResponseStatus }
                ?.let { it as ResponseStatus }
                ?.value
                ?: HttpStatus.INTERNAL_SERVER_ERROR
        if (throwable is JwtTokenUnauthorizedException) {
            logger.warn("JwtTokenUnauthorizedException - ${throwable.cause?.javaClass?.simpleName}")
        } else {
            val feilmelding = "Ukjent feil throwable=${rootCause(throwable)} status=$responseStatus"
            logger.error(feilmelding)
            secureLogger.error(feilmelding, throwable)
        }

        return ProblemDetail.forStatusAndDetail(responseStatus, "Ukjent feil")
    }

    /**
     * Gir 503 når et annet system er midlertidig utilgjengelig og brukeren trygt kan prøve igjen.
     * Andre feil håndteres som ukjente feil.
     */
    @ExceptionHandler(ResourceAccessException::class, HttpServerErrorException::class)
    fun handleKallMotAnnetSystemFeilet(throwable: Exception): ProblemDetail {
        if (!kanPrøveIgjen(throwable)) {
            return handleThrowable(throwable)
        }
        logger.warn("Annet system er midlertidig utilgjengelig throwable=${rootCause(throwable)}")
        secureLogger.warn("Annet system er midlertidig utilgjengelig", throwable)
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Kommunikasjonsproblemer med andre systemer - prøv igjen",
        )
    }

    // Ved connect-timeout nådde ikke kallet fram. Ved read-timeout kan det ha gått gjennom, og da er det ikke trygt å prøve igjen.
    private fun kanPrøveIgjen(throwable: Exception): Boolean =
        when (throwable) {
            is HttpServerErrorException -> throwable.statusCode.value() in 502..504
            else -> NestedExceptionUtils.getMostSpecificCause(throwable) is ConnectTimeoutException
        }

    @ExceptionHandler(SøknadValideringException::class)
    fun handleThrowable(throwable: SøknadValideringException): ProblemDetail {
        secureLogger.warn("Valideringsfeil: ${throwable.message}")
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, throwable.message)
    }

    @ExceptionHandler(GradertBrukerException::class)
    fun handleThrowable(throwable: GradertBrukerException): ProblemDetail {
        secureLogger.warn("Brukeren inneholder barn som har høyere gradering enn søker status=400")
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "ROUTING_GAMMEL_SØKNAD")
    }
}
