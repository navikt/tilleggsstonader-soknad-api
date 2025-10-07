package no.nav.tilleggsstonader.soknad.infrastruktur

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.tilleggsstonader.kontrakter.dokarkiv.ArkiverDokumentResponse
import no.nav.tilleggsstonader.soknad.dokument.FamilieDokumentClient
import no.nav.tilleggsstonader.soknad.util.FileUtil.readBytes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.util.UUID

@Configuration
@Profile("mock-integrasjoner")
class IntegrasjonerClientConfig {
    @Bean
    fun integrasjonerClient(): IntegrasjonerClient {
        val client = mockk<IntegrasjonerClient>()
        resetIntegrasjonerClientMock(client)
        return client
    }

    companion object {
        fun resetIntegrasjonerClientMock(client: IntegrasjonerClient) {
            clearMocks(client)
            every { client.arkiver(any()) } returns
                ArkiverDokumentResponse(UUID.randomUUID().toString(), true, emptyList())
        }
    }
}
