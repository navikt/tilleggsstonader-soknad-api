package no.nav.tilleggsstonader.soknad

import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.tilleggsstonader.soknad.util.DbContainerInitializer
import org.springframework.boot.builder.SpringApplicationBuilder

/**
 * Denne settes til en fixed port for å kunne bruke samme port som familie-dokument
 */
private val mockOauth2ServerPort: String = "11588"

@EnableMockOAuth2Server
class SøknadApiLocal : App()

fun main(args: Array<String>) {
    SpringApplicationBuilder(SøknadApiLocal::class.java)
        .initializers(DbContainerInitializer())
        .profiles(
            "local",
            "mock-pdl",
            "mock-vedlegg",
            "mock-sak",
            "mock-familie-vedlegg-controller",
        )
        .properties(mapOf("mock-oauth2-server.port" to mockOauth2ServerPort))
        .run(*args)
}
