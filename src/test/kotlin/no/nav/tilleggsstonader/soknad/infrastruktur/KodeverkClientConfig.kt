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
        every { client.hentPostnummer() } returns
            KodeverkDto(
                listOf("0100" to "Oslo").tilBetydninger(),
            )
        return client
    }

    private fun List<Pair<String, String>>.tilBetydninger() =
        this.associate { it.first to listOf(betydning(BeskrivelseDto(it.second, it.second))) }

    private fun betydning(it: BeskrivelseDto) =
        BetydningDto(
            gyldigFra = LocalDate.now().minusYears(1),
            gyldigTil = LocalDate.now().plusYears(1),
            beskrivelser = mapOf("nb" to it),
        )
}
