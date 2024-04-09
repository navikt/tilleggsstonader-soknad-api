package no.nav.tilleggsstonader.soknad.infrastruktur

import io.mockk.every
import io.mockk.mockk
import no.nav.tilleggsstonader.soknad.dokument.FamilieDokumentClient
import no.nav.tilleggsstonader.soknad.util.FileUtil.readBytes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("mock-integrasjoner")
class IntegrasjonerClientConfig {

    @Bean
    fun familieDokumentClient(): FamilieDokumentClient {
        val client = mockk<FamilieDokumentClient>()
        val dummyPdf = readBytes("dummy/pdf_dummy.pdf")
        every { client.genererPdf(any()) } returns dummyPdf
        return client
    }
}
