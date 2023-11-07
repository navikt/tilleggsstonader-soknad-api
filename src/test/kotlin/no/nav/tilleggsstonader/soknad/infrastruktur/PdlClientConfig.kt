package no.nav.tilleggsstonader.soknad.infrastruktur

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.tilleggsstonader.libs.test.fnr.FnrGenerator
import no.nav.tilleggsstonader.libs.utils.fnr.Fødselsnummer
import no.nav.tilleggsstonader.soknad.person.pdl.PdlClient
import no.nav.tilleggsstonader.soknad.person.pdl.PdlClientCredentialClient
import no.nav.tilleggsstonader.soknad.person.pdl.dto.Adressebeskyttelse
import no.nav.tilleggsstonader.soknad.person.pdl.dto.AdressebeskyttelseGradering
import no.nav.tilleggsstonader.soknad.person.pdl.dto.Bostedsadresse
import no.nav.tilleggsstonader.soknad.person.pdl.dto.ForelderBarnRelasjon
import no.nav.tilleggsstonader.soknad.person.pdl.dto.Fødsel
import no.nav.tilleggsstonader.soknad.person.pdl.dto.Navn
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlBarn
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlSøker
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import java.time.Year

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

    @Bean
    @Primary
    fun pdlClientCredentialClient(): PdlClientCredentialClient {
        val client = mockk<PdlClientCredentialClient>()
        resetPdlClientMock(client)
        return client
    }

    companion object {
        fun resetPdlClientMock(client: PdlClient) {
            clearMocks(client)
            every { client.hentSøker(any()) } returns lagPdlSøker()
        }

        fun resetPdlClientMock(client: PdlClientCredentialClient) {
            clearMocks(client)
            val år = Year.now().value
            val barn1 = lagPdlBarn(
                ident = FnrGenerator.generer(år - 5, 5, 12),
                navn = navn(fornavn = "Ronja", etternavn = "Røverdatter")
            )
            val barn2 = lagPdlBarn(
                ident = FnrGenerator.generer(år - 11, 1, 5),
                navn = navn(fornavn = "Espen", etternavn = "Askeladden")
            )
            every { client.hentBarn(any()) } returns listOf(barn1, barn2).toMap()
        }
    }
}

private val ugradert = Adressebeskyttelse(AdressebeskyttelseGradering.UGRADERT)

fun navn(
    fornavn: String = "fornavn",
    mellomnavn: String? = null,
    etternavn: String = "etternavn"
) = Navn(fornavn, mellomnavn, etternavn)

fun lagPdlSøker(
    adressebeskyttelse: Adressebeskyttelse = ugradert,
    bostedsadresse: Bostedsadresse = Bostedsadresse(null, null),
    forelderBarnRelasjon: List<ForelderBarnRelasjon> = emptyList(),
    navn: Navn = navn(),
) = PdlSøker(
    adressebeskyttelse = listOf(adressebeskyttelse),
    bostedsadresse = listOf(bostedsadresse),
    forelderBarnRelasjon = forelderBarnRelasjon,
    navn = listOf(navn),
)

fun lagPdlBarn(
    ident: String,
    adressebeskyttelse: Adressebeskyttelse = ugradert,
    navn: Navn = navn(),
): Pair<String, PdlBarn> {
    val fødselsdato = Fødselsnummer(ident).fødselsdato
    return Pair(
        ident,
        PdlBarn(
            adressebeskyttelse = listOf(adressebeskyttelse),
            navn = listOf(navn),
            fødsel = listOf(Fødsel(fødselsår = fødselsdato.year, fødselsdato = fødselsdato)),
            dødsfall = emptyList()
        )
    )
}
