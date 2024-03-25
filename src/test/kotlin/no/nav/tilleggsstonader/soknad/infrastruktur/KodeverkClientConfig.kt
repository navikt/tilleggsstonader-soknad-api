package no.nav.tilleggsstonader.soknad.infrastruktur

import io.mockk.every
import io.mockk.mockk
import no.nav.tilleggsstonader.kontrakter.kodeverk.BeskrivelseDto
import no.nav.tilleggsstonader.kontrakter.kodeverk.BetydningDto
import no.nav.tilleggsstonader.kontrakter.kodeverk.KodeverkDto
import no.nav.tilleggsstonader.soknad.kodeverk.KodeverkClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.time.LocalDate

@Configuration
@Profile("mock-kodeverk")
class KodeverkClientConfig {

    @Bean
    fun kodeverkClient(): KodeverkClient {
        val client = mockk<KodeverkClient>()
        val fom = LocalDate.now().minusYears(3)
        val tom = LocalDate.now().plusYears(3)
        every { client.hentPostnummer() } returns KodeverkDto(
            mapOf(
                "Postnummer" to listOf(
                    BetydningDto(
                        fom,
                        tom,
                        mapOf("nb" to BeskrivelseDto("0010", "Oslo")),
                    ),
                ),
            ),
        )
        every { client.hentLandkoder() } returns KodeverkDto(
            mapOf(
                "Landkoder" to listOf(
                    BetydningDto(
                        fom,
                        tom,
                        mapOf("nb" to BeskrivelseDto("SWE", "SVERIGE")),
                    ),
                    BetydningDto(
                        fom,
                        tom,
                        mapOf("nb" to BeskrivelseDto("NOR", "NORGE")),
                    ),
                ),
            ),
        )

        return client
    }
}
