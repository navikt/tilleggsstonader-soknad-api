package no.nav.tilleggsstonader.soknad.infrastruktur

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.tilleggsstonader.libs.utils.fnr.Fødselsnummer
import no.nav.tilleggsstonader.soknad.person.pdl.PdlClient
import no.nav.tilleggsstonader.soknad.person.pdl.PdlClientCredentialClient
import no.nav.tilleggsstonader.soknad.person.pdl.dto.Adressebeskyttelse
import no.nav.tilleggsstonader.soknad.person.pdl.dto.AdressebeskyttelseGradering
import no.nav.tilleggsstonader.soknad.person.pdl.dto.Bostedsadresse
import no.nav.tilleggsstonader.soknad.person.pdl.dto.ForelderBarnRelasjon
import no.nav.tilleggsstonader.soknad.person.pdl.dto.Fødselsdato
import no.nav.tilleggsstonader.soknad.person.pdl.dto.Navn
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlBarn
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlSøker
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlSøkerNavn
import no.nav.tilleggsstonader.soknad.person.pdl.dto.Vegadresse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import java.time.LocalDate

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
            val barn1 =
                lagPdlBarn(
                    ident = "08921997974",
                    navn = navn(fornavn = "Ronja", etternavn = "Røverdatter"),
                )
            val barn2 =
                lagPdlBarn(
                    ident = "43921075201",
                    navn = navn(fornavn = "Espen", etternavn = "Askeladden"),
                )
            every { client.hentBarn(any()) } returns listOf(barn1, barn2).toMap()

            every { client.hentNavn(any()) } returns PdlSøkerNavn(listOf(navn()))
        }
    }
}

fun navn(
    fornavn: String = "fornavn",
    mellomnavn: String? = null,
    etternavn: String = "etternavn",
) = Navn(fornavn, mellomnavn, etternavn)

fun lagPdlSøker(
    adressebeskyttelse: AdressebeskyttelseGradering = AdressebeskyttelseGradering.UGRADERT,
    bostedsadresse: Bostedsadresse = Bostedsadresse(vegadresse(), null),
    forelderBarnRelasjon: List<ForelderBarnRelasjon> = emptyList(),
    navn: Navn = navn(),
    fødselsdato: Fødselsdato = Fødselsdato(fødselsår = 2005, fødselsdato = LocalDate.of(2005, 1, 1)),
) = PdlSøker(
    adressebeskyttelse = listOf(Adressebeskyttelse(adressebeskyttelse)),
    bostedsadresse = listOf(bostedsadresse),
    forelderBarnRelasjon = forelderBarnRelasjon,
    navn = listOf(navn),
    fødselsdato = listOf(fødselsdato),
)

private fun vegadresse() =
    Vegadresse(
        husnummer = "3",
        husbokstav = "a",
        bruksenhetsnummer = null,
        adressenavn = "Hildes vei",
        postnummer = "0100",
    )

fun lagPdlBarn(
    ident: String,
    adressebeskyttelse: AdressebeskyttelseGradering = AdressebeskyttelseGradering.UGRADERT,
    navn: Navn = navn(),
): Pair<String, PdlBarn> {
    val fødselsdato = Fødselsnummer(ident).fødselsdato
    return Pair(
        ident,
        PdlBarn(
            adressebeskyttelse = listOf(Adressebeskyttelse(adressebeskyttelse)),
            navn = listOf(navn),
            fødselsdato = listOf(Fødselsdato(fødselsår = fødselsdato.year, fødselsdato = fødselsdato)),
            dødsfall = emptyList(),
        ),
    )
}
