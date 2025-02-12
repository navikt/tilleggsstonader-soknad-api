package no.nav.tilleggsstonader.soknad.infrastruktur

import io.mockk.every
import io.mockk.mockk
import no.nav.tilleggsstonader.soknad.dokument.FamilieVedleggClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("mock-vedlegg")
class FamilieVedleggClientConfig {
    @Bean
    fun familieVedleggClient(): FamilieVedleggClient {
        val client = mockk<FamilieVedleggClient>()
        every { client.hentVedlegg(any()) } returns byteArrayOf(13)
        return client
    }
}
