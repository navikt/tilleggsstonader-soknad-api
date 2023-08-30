package no.nav.tilleggsstonader.soknad.infrastruktur

import com.nimbusds.jose.JOSEObjectType
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import no.nav.security.token.support.core.api.Unprotected
import no.nav.security.token.support.spring.test.MockLoginController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("test")
class TokenCookieController(
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    val mockOAuth2Server: MockOAuth2Server,
) {
    @Unprotected
    @GetMapping("/cookie")
    fun addCookie(
        @RequestParam(value = "issuerId", defaultValue = "tokenx") issuerId: String,
        @RequestParam(value = "audience", defaultValue = "tilleggsstonader-app") audience: String,
        @RequestParam(value = "subject", defaultValue = "12345678910") subject: String,
        @RequestParam(value = "cookiename", defaultValue = "localhost-idtoken") cookieName: String,
        @RequestParam(value = "redirect", required = false) redirect: String?,
        @RequestParam(value = "expiry", required = false) expiry: String?,
        response: HttpServletResponse,
    ): Cookie? {
        val token = mockOAuth2Server.issueToken(
            issuerId,
            MockLoginController::class.java.simpleName,
            DefaultOAuth2TokenCallback(
                issuerId = issuerId,
                subject = subject,
                typeHeader = JOSEObjectType.JWT.type,
                audience = listOf(audience),
                claims = mapOf(
                    "acr" to "Level4",
                    "pid" to subject,
                ),
                expiry = expiry?.toLong() ?: 3600,
            ),
        ).serialize()
        return createCookieAndAddToResponse(
            response,
            cookieName,
            token,
            redirect,
        )
    }

    private fun createCookieAndAddToResponse(
        response: HttpServletResponse,
        cookieName: String,
        token: String,
        redirect: String?,
    ): Cookie? {
        val cookie = Cookie(cookieName, token)
        cookie.domain = "localhost"
        cookie.path = "/"
        response.addCookie(cookie)
        if (redirect != null) {
            response.sendRedirect(redirect)
            return null
        }
        return cookie
    }
}
