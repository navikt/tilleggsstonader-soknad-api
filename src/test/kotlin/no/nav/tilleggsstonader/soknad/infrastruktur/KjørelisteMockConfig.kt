package no.nav.tilleggsstonader.soknad.infrastruktur

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import no.nav.tilleggsstonader.kontrakter.søknad.DatoFelt
import no.nav.tilleggsstonader.kontrakter.søknad.VerdiFelt
import no.nav.tilleggsstonader.soknad.kjøreliste.KjørelisteDto
import no.nav.tilleggsstonader.soknad.kjøreliste.KjørelisteService
import no.nav.tilleggsstonader.soknad.kjøreliste.ReisedagDto
import no.nav.tilleggsstonader.soknad.kjøreliste.UkeMedReisedagerDto
import no.nav.tilleggsstonader.soknad.sak.DagligReisePrivatBilClient
import no.nav.tilleggsstonader.soknad.soknad.SkjemaService
import no.nav.tilleggsstonader.soknad.soknad.SøknadMetadataDto
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
        private fun kjørelisteDtoForReise1(): KjørelisteDto =
            KjørelisteDto(
                reiseId = "1",
                reisedagerPerUkeAvsnitt =
                    listOf(
                        UkeMedReisedagerDto(
                            ukeLabel = "Uke 1",
                            reisedagerLabel = "Ukentlige reisedager: 3",
                            spørsmål = "Hvilke dager kjørte du?",
                            reisedager =
                                listOf(
                                    reisedag(dato = LocalDate.of(2025, 1, 1), harKjørt = true, parkering = 50),
                                    reisedag(dato = LocalDate.of(2025, 1, 2), harKjørt = true, parkering = 0),
                                    reisedag(dato = LocalDate.of(2025, 1, 3), harKjørt = true, parkering = 30),
                                    reisedag(dato = LocalDate.of(2025, 1, 4), harKjørt = false, parkering = null),
                                    reisedag(dato = LocalDate.of(2025, 1, 5), harKjørt = false, parkering = null),
                                ),
                        ),
                    ),
                dokumentasjon = emptyList(),
                søknadMetadata = SøknadMetadataDto(søknadFrontendGitHash = null),
            )

        private fun reisedag(
            dato: LocalDate,
            harKjørt: Boolean,
            parkering: Number?,
        ): ReisedagDto =
            ReisedagDto(
                dato = DatoFelt(label = dato.toString(), verdi = dato),
                harKjørt = harKjørt,
                parkeringsutgift = VerdiFelt(label = "Parkeringsutgift (kr)", verdi = parkering),
            )
    }
}
