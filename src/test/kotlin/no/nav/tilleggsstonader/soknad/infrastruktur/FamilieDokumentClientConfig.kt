package no.nav.tilleggsstonader.soknad.infrastruktur

import io.mockk.every
import io.mockk.mockk
import no.nav.tilleggsstonader.kontrakter.dokarkiv.ArkiverDokumentResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.util.UUID

@Configuration
@Profile("mock-dokument")
class FamilieDokumentClientConfig {

    @Bean
    fun integrasjonerClient(): IntegrasjonerClient {
        val client = mockk<IntegrasjonerClient>()
        every { client.arkiver(any()) } returns
            ArkiverDokumentResponse(UUID.randomUUID().toString(), true, emptyList())
        return client
    }
}
