package no.nav.tilleggsstonader.soknad.infrastruktur

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.tilleggsstonader.kontrakter.søknad.RammevedtakDto
import no.nav.tilleggsstonader.kontrakter.søknad.RammevedtakUkeDto
import no.nav.tilleggsstonader.soknad.sak.DagligReisePrivatBilClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import java.time.LocalDate

@Configuration
@Profile("mock-rammevedtak")
class DagligReisePrivatBilClientConfig {
    @Bean
    @Primary
    fun dagligReisePrivatBilClient(): DagligReisePrivatBilClient {
        val client = mockk<DagligReisePrivatBilClient>()
        resetMock(client)
        return client
    }

    companion object {
        fun resetMock(client: DagligReisePrivatBilClient) {
            clearMocks(client)
            every { client.hentRammevedtakForInnloggetBruker() } returns
                listOf(
                    RammevedtakDto(
                        reiseId = "1",
                        fom = LocalDate.of(2025, 1, 1),
                        tom = LocalDate.of(2025, 2, 6),
                        reisedagerPerUke = 3,
                        aktivitetsadresse = "Tiurveien 34, 0356 Oslo",
                        aktivitetsnavn = "Arbeidstrening",
                        uker =
                            listOf(
                                RammevedtakUkeDto(
                                    fom = LocalDate.of(2025, 1, 1),
                                    tom = LocalDate.of(2025, 1, 5),
                                    ukeNummer = 1,
                                    innsendtDato = LocalDate.of(2025, 1, 3),
                                    kanSendeInnKjøreliste = true,
                                ),
                                RammevedtakUkeDto(
                                    fom = LocalDate.of(2025, 1, 6),
                                    tom = LocalDate.of(2025, 1, 12),
                                    ukeNummer = 2,
                                    innsendtDato = null,
                                    kanSendeInnKjøreliste = true,
                                ),
                                RammevedtakUkeDto(
                                    fom = LocalDate.of(2025, 1, 13),
                                    tom = LocalDate.of(2025, 1, 19),
                                    ukeNummer = 3,
                                    innsendtDato = null,
                                    kanSendeInnKjøreliste = true,
                                ),
                                RammevedtakUkeDto(
                                    fom = LocalDate.of(2025, 1, 20),
                                    tom = LocalDate.of(2025, 1, 26),
                                    ukeNummer = 4,
                                    innsendtDato = null,
                                    kanSendeInnKjøreliste = true,
                                ),
                                RammevedtakUkeDto(
                                    fom = LocalDate.of(2025, 1, 27),
                                    tom = LocalDate.of(2025, 2, 2),
                                    ukeNummer = 5,
                                    innsendtDato = null,
                                    kanSendeInnKjøreliste = false,
                                ),
                                RammevedtakUkeDto(
                                    fom = LocalDate.of(2025, 2, 3),
                                    tom = LocalDate.of(2025, 2, 6),
                                    ukeNummer = 6,
                                    innsendtDato = null,
                                    kanSendeInnKjøreliste = false,
                                ),
                            ),
                    ),
                    RammevedtakDto(
                        reiseId = "2",
                        fom = LocalDate.of(2025, 2, 10),
                        tom = LocalDate.of(2025, 2, 16),
                        reisedagerPerUke = 3,
                        aktivitetsadresse = "Drammensveien 1, 0356 Oslo",
                        aktivitetsnavn = "Tiltak",
                        uker =
                            listOf(
                                RammevedtakUkeDto(
                                    fom = LocalDate.of(2025, 2, 10),
                                    tom = LocalDate.of(2025, 2, 16),
                                    ukeNummer = 7,
                                    innsendtDato = null,
                                    kanSendeInnKjøreliste = false,
                                ),
                            ),
                    ),
                )
        }
    }
}
