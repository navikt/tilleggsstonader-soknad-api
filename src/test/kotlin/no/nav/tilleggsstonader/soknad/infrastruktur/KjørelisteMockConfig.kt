package no.nav.tilleggsstonader.soknad.infrastruktur

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import no.nav.tilleggsstonader.soknad.kjøreliste.KjørelisteService
import no.nav.tilleggsstonader.soknad.kjøreliste.KjørelisteVisningDto
import no.nav.tilleggsstonader.soknad.kjøreliste.ReisedagVisningDto
import no.nav.tilleggsstonader.soknad.sak.DagligReisePrivatBilClient
import no.nav.tilleggsstonader.soknad.soknad.SkjemaService
import no.nav.tilleggsstonader.soknad.soknad.domene.SkjemaRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import java.time.LocalDate

@Configuration
@Profile("mock-kjoreliste")
class KjørelisteMockConfig {
    @Bean
    @Primary
    fun kjørelisteService(): KjørelisteService {
        val skjemaService = mockk<SkjemaService>(relaxed = true)
        val dagligReisePrivatBilClient = mockk<DagligReisePrivatBilClient>()
        val skjemaRepository = mockk<SkjemaRepository>(relaxed = true)
        DagligReisePrivatBilClientConfig.resetMock(dagligReisePrivatBilClient)

        val service = spyk(KjørelisteService(skjemaService, dagligReisePrivatBilClient, skjemaRepository))
        every { service.hentKjørelisterForReise("1") } returns kjørelisteDtoForReise1()
        every { service.hentKjørelisterForReise("2") } returns null
        return service
    }

    companion object {
        /**
         * Mock-data basert på rammevedtak "1" i DagligReisePrivatBilClientConfig.
         * Uke 1 (1. jan - 5. jan 2025) har innsendtDato satt, så vi returnerer kjøreliste for den uken.
         */
        private fun kjørelisteDtoForReise1(): KjørelisteVisningDto =
            KjørelisteVisningDto(
                reisedager =
                    listOf<ReisedagVisningDto>(
                        ReisedagVisningDto(
                            dato = LocalDate.of(2025, 1, 1),
                            harKjørt = true,
                            parkeringsutgift = 50,
                        ),
                        ReisedagVisningDto(
                            dato = LocalDate.of(2025, 1, 2),
                            harKjørt = true,
                            parkeringsutgift = 0,
                        ),
                        ReisedagVisningDto(
                            dato = LocalDate.of(2025, 1, 3),
                            harKjørt = true,
                            parkeringsutgift = 30,
                        ),
                        ReisedagVisningDto(
                            dato = LocalDate.of(2025, 1, 4),
                            harKjørt = false,
                            parkeringsutgift = null,
                        ),
                        ReisedagVisningDto(
                            dato = LocalDate.of(2025, 1, 5),
                            harKjørt = false,
                            parkeringsutgift = null,
                        ),
                    ),
            )
    }
}
