package no.nav.tilleggsstonader.soknad.person

import no.nav.tilleggsstonader.libs.utils.fnr.Fødselsnummer
import no.nav.tilleggsstonader.soknad.person.dto.PersonMedBarnDto
import no.nav.tilleggsstonader.soknad.person.pdl.PdlClient
import no.nav.tilleggsstonader.soknad.person.pdl.dto.Familierelasjonsrolle
import org.springframework.stereotype.Service

@Service
class PersonService(
    private val pdlClient: PdlClient,
) {

    fun hentSøker(fødselsnummer: Fødselsnummer): PersonMedBarnDto {
        val søker = pdlClient.hentSøker(fødselsnummer)
        return PersonMedBarnDto(
            navn = søker.navn.first().visningsnavn(),
            barn = søker.forelderBarnRelasjon.filter { it.relatertPersonsRolle == Familierelasjonsrolle.BARN }.map { it.relatertPersonsIdent!! },
        )
    }
}
