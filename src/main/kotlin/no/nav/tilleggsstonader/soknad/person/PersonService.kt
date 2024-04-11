package no.nav.tilleggsstonader.soknad.person

import no.nav.tilleggsstonader.libs.utils.fnr.Fødselsnummer
import no.nav.tilleggsstonader.soknad.infrastruktur.exception.GradertBrukerException
import no.nav.tilleggsstonader.soknad.person.dto.Barn
import no.nav.tilleggsstonader.soknad.person.dto.PersonMedBarnDto
import no.nav.tilleggsstonader.soknad.person.pdl.PdlClient
import no.nav.tilleggsstonader.soknad.person.pdl.PdlClientCredentialClient
import no.nav.tilleggsstonader.soknad.person.pdl.dto.Familierelasjonsrolle
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlBarn
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlSøker
import no.nav.tilleggsstonader.soknad.person.pdl.erStrengtFortrolig
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.Period

@Service
class PersonService(
    private val pdlClient: PdlClient,
    private val pdlClientCredentialClient: PdlClientCredentialClient,
    private val adresseMapper: AdresseMapper,
) {

    fun hentSøker(fødselsnummer: Fødselsnummer): PersonMedBarnDto {
        val søker = pdlClient.hentSøker(fødselsnummer)
        val barn = hentBarn(søker)

        if (søkerEllerBarnErGradert(søker, barn)) {
            throw GradertBrukerException()
        }

        return PersonMedBarnDto(
            fornavn = søker.navn.first().fornavn,
            visningsnavn = søker.navn.first().visningsnavn(),
            adresse = adresseMapper.tilFormatertAdresse(søker),
            barn = mapBarn(barn),
        )
    }

    private fun mapBarn(barn: Map<String, PdlBarn>) =
        barn.entries
            .filter { erILive(it.value) }
            .map { (ident, pdlBarn) ->
                val fødselsdato = pdlBarn.fødsel.firstOrNull()?.fødselsdato ?: error("Ingen fødselsdato registrert")
                val alder = Period.between(fødselsdato, LocalDate.now()).years
                Barn(
                    ident = ident,
                    fornavn = pdlBarn.navn.first().fornavn,
                    visningsnavn = pdlBarn.navn.first().visningsnavn(),
                    fødselsdato = fødselsdato,
                    alder = alder,
                )
            }.sortedBy { it.alder }

    private fun erILive(pdlBarn: PdlBarn) =
        pdlBarn.dødsfall.firstOrNull()?.dødsdato == null

    private fun hentBarn(søker: PdlSøker): Map<String, PdlBarn> {
        val barnIdenter = søker.forelderBarnRelasjon.filter { it.relatertPersonsRolle == Familierelasjonsrolle.BARN }
            .mapNotNull { it.relatertPersonsIdent }
        return pdlClientCredentialClient.hentBarn(barnIdenter)
    }

    private fun søkerEllerBarnErGradert(
        søker: PdlSøker,
        barn: Map<String, PdlBarn>,
    ) =
        søker.adressebeskyttelse.erStrengtFortrolig() ||
            barn.values.any { it.adressebeskyttelse.erStrengtFortrolig() }
}
