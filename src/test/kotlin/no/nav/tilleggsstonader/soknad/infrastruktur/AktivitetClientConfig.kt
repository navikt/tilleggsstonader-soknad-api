package no.nav.tilleggsstonader.soknad.infrastruktur

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.tilleggsstonader.soknad.aktivitet.AktivitetClient
import no.nav.tilleggsstonader.soknad.util.AktivitetArenaDtoUtil.aktivitetArenaDto
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
@Profile("mock-aktivitet")
class AktivitetClientConfig {

    @Bean
    @Primary
    fun aktivitetClient(): AktivitetClient {
        val client = mockk<AktivitetClient>()

        resetMock(client)

        return client
    }

    companion object {
        fun resetMock(client: AktivitetClient) {
            clearMocks(client)
            every { client.hentAktiviteter(any(), any(), any()) } returns listOf(aktivitetArenaDto("1"))
        }
    }
}
