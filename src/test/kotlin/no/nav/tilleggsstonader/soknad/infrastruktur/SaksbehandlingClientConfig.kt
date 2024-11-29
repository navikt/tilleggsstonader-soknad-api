package no.nav.tilleggsstonader.soknad.infrastruktur

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import no.nav.tilleggsstonader.soknad.sak.SaksbehandlingClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
@Profile("mock-sak")
class SaksbehandlingClientConfig {

    @Primary
    @Bean
    fun saksbehandlingClient(): SaksbehandlingClient {
        val client = mockk<SaksbehandlingClient>()
        every { client.skalRoutesTilNyLøsning(any()) } answers { true }
        justRun { client.sendTilSak(any()) }
        every { client.hentBehandlingStatus(any()) } answers { false }
        return client
    }
}
