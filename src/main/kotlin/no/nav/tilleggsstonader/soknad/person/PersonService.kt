package no.nav.tilleggsstonader.soknad.person

import no.nav.tilleggsstonader.libs.utils.fnr.Fødselsnummer
import no.nav.tilleggsstonader.soknad.infrastruktur.exception.GradertBrukerException
import no.nav.tilleggsstonader.soknad.person.dto.Adresse
import no.nav.tilleggsstonader.soknad.person.dto.Barn
import no.nav.tilleggsstonader.soknad.person.dto.PersonMedBarnDto
import no.nav.tilleggsstonader.soknad.person.pdl.PdlClient
import no.nav.tilleggsstonader.soknad.person.pdl.PdlClientCredentialClient
import no.nav.tilleggsstonader.soknad.person.pdl.dto.AdressebeskyttelseGradering
import no.nav.tilleggsstonader.soknad.person.pdl.dto.Familierelasjonsrolle
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlBarn
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlSøker
import no.nav.tilleggsstonader.soknad.person.pdl.gradering
import no.nav.tilleggsstonader.soknad.util.EnvUtil
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class PersonService(
    private val pdlClient: PdlClient,
    private val pdlClientCredentialClient: PdlClientCredentialClient,
) {

    fun hentSøker(fødselsnummer: Fødselsnummer): PersonMedBarnDto {
        val søker = pdlClient.hentSøker(fødselsnummer)
        val barn = hentBarn(søker)

        if (søkerEllerBarnErGradert(søker, barn)) {
            throw GradertBrukerException()
        }
        // TODO fjern mockdata
        if (EnvUtil.erIProd()) {
            error("Fikse mocket data")
        }

        return PersonMedBarnDto(
            navn = søker.navn.first().visningsnavn(),
            adresse = Adresse(
                adresse = "Liaveien 34",
                postnummer = "0152",
                poststed = "Oslo",
            ),
            telefonnr = "950863265",
            epost = "mail@gmail.com",
            kontonr = "1234.56.78910",
            barn = barn.entries.map {
                Barn(
                    ident = it.key,
                    navn = it.value.navn.first().visningsnavn(),
                    fødselsdato = LocalDate.of(2023, 1, 1),
                    alder = 9,
                )
            },
        )
    }

    private fun hentBarn(søker: PdlSøker): Map<String, PdlBarn> {
        val barnIdenter = søker.forelderBarnRelasjon.filter { it.relatertPersonsRolle == Familierelasjonsrolle.BARN }
            .mapNotNull { it.relatertPersonsIdent }
        return pdlClientCredentialClient.hentBarn(barnIdenter)
    }

    private fun søkerEllerBarnErGradert(
        søker: PdlSøker,
        barn: Map<String, PdlBarn>
    ) =
        søker.adressebeskyttelse.gradering() != AdressebeskyttelseGradering.UGRADERT ||
                barn.values.any { it.adressebeskyttelse.gradering() != AdressebeskyttelseGradering.UGRADERT }
}
