package no.nav.tilleggsstonader.soknad.infrastruktur

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.tilleggsstonader.soknad.person.pdl.PdlClient
import no.nav.tilleggsstonader.soknad.person.pdl.dto.Adressebeskyttelse
import no.nav.tilleggsstonader.soknad.person.pdl.dto.Bostedsadresse
import no.nav.tilleggsstonader.soknad.person.pdl.dto.ForelderBarnRelasjon
import no.nav.tilleggsstonader.soknad.person.pdl.dto.Navn
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlSøker
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
@Profile("mock-pdl")
class PdlClientConfig {

    @Bean
    @Primary
    fun pdlClient(): PdlClient {
        val client = mockk<PdlClient>()
        resetPdlClientMock(client)
        return client
    }

    companion object {
        fun resetPdlClientMock(client: PdlClient) {
            clearMocks(client)
            every { client.hentSøker(any()) } returns lagPdlSøker()
        }
    }
}

fun lagPdlSøker(
    adressebeskyttelse: List<Adressebeskyttelse> = emptyList(),
    bostedsadresse: List<Bostedsadresse> = emptyList(),
    forelderBarnRelasjon: List<ForelderBarnRelasjon> = emptyList(),
    navn: List<Navn> = listOf(Navn("fornavn", null, "etternavn")),
) = PdlSøker(
    adressebeskyttelse = adressebeskyttelse,
    bostedsadresse = bostedsadresse,
    forelderBarnRelasjon = forelderBarnRelasjon,
    navn = navn,
)
