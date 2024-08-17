package no.nav.tilleggsstonader.soknad.person

import no.nav.tilleggsstonader.libs.log.SecureLogger.secureLogger
import no.nav.tilleggsstonader.libs.utils.fnr.Fødselsnummer
import no.nav.tilleggsstonader.libs.utils.osloDateNow
import no.nav.tilleggsstonader.soknad.person.dto.Barn
import no.nav.tilleggsstonader.soknad.person.dto.PersonMedBarnDto
import no.nav.tilleggsstonader.soknad.person.pdl.PdlClient
import no.nav.tilleggsstonader.soknad.person.pdl.PdlClientCredentialClient
import no.nav.tilleggsstonader.soknad.person.pdl.dto.AdressebeskyttelseGradering
import no.nav.tilleggsstonader.soknad.person.pdl.dto.Familierelasjonsrolle
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlBarn
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlSøker
import no.nav.tilleggsstonader.soknad.person.pdl.gradering
import org.springframework.stereotype.Service
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

        return PersonMedBarnDto(
            fornavn = søker.navn.first().fornavn,
            visningsnavn = søker.navn.first().visningsnavn(),
            adresse = adresseMapper.tilFormatertAdresse(søker),
            barn = mapBarn(søker, barn),
        )
    }

    /**
     * Skal kun brukes for å hente navn til søknad-pdf, og gjøres uten context av bruker
     */
    fun hentNavnMedClientCredential(ident: String): String {
        return pdlClientCredentialClient.hentNavn(ident).navn.first().visningsnavn()
    }

    private fun mapBarn(søker: PdlSøker, barn: Map<String, PdlBarn>): List<Barn> {
        val søkersGradering = søker.adressebeskyttelse.gradering()

        return barn.entries
            .filter { erILive(it.value) }
            .filter { harLikEllerLavereGradering(it.value, søkersGradering) }
            .map { (ident, pdlBarn) -> mapBarn(ident, pdlBarn) }
            .sortedBy { it.alder }
    }

    private fun mapBarn(
        ident: String,
        pdlBarn: PdlBarn,
    ): Barn {
        val fødselsdato =
            pdlBarn.fødselsdato.firstOrNull()?.fødselsdato ?: error("Ingen fødselsdato registrert")
        val alder = Period.between(fødselsdato, osloDateNow()).years
        return Barn(
            ident = ident,
            fornavn = pdlBarn.navn.first().fornavn,
            visningsnavn = pdlBarn.navn.first().visningsnavn(),
            fødselsdato = fødselsdato,
            alder = alder,
        )
    }

    private fun harLikEllerLavereGradering(
        barn: PdlBarn,
        søkersGradering: AdressebeskyttelseGradering,
    ): Boolean {
        val harLikEllerLavereGradering = barn.adressebeskyttelse.gradering().nivå <= søkersGradering.nivå
        if (!harLikEllerLavereGradering) {
            secureLogger.info("Filtrer bort barn pga gradering")
        }
        return harLikEllerLavereGradering
    }

    private fun erILive(pdlBarn: PdlBarn) =
        pdlBarn.dødsfall.firstOrNull()?.dødsdato == null

    private fun hentBarn(søker: PdlSøker): Map<String, PdlBarn> {
        val barnIdenter = søker.forelderBarnRelasjon.filter { it.relatertPersonsRolle == Familierelasjonsrolle.BARN }
            .mapNotNull { it.relatertPersonsIdent }
        return pdlClientCredentialClient.hentBarn(barnIdenter)
    }
}
