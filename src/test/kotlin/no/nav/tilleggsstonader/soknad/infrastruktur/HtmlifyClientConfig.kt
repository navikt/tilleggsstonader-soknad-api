package no.nav.tilleggsstonader.soknad.infrastruktur

import io.mockk.every
import io.mockk.mockk
import no.nav.tilleggsstonader.soknad.dokument.HtmlifyClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("mock-htmlify")
class HtmlifyClientConfig {
    @Bean
    fun htmlifyClient(): HtmlifyClient {
        val client = mockk<HtmlifyClient>()
        every { client.genererSøknadHtml(any(), any(), any(), any(), any(), any()) } returns "<h1>Hei</h1>"
        every { client.genererKjørelisteHtml(any()) } returns "<h1>Hei kjøreliste</h1>"
        return client
    }
}
