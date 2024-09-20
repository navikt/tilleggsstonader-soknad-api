package no.nav.tilleggsstonader.soknad.person

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tilleggsstonader.libs.sikkerhet.EksternBrukerUtils
import no.nav.tilleggsstonader.libs.utils.fnr.Fødselsnummer
import no.nav.tilleggsstonader.soknad.person.dto.PersonMedBarnDto
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/person")
@ProtectedWithClaims(issuer = EksternBrukerUtils.ISSUER_TOKENX, claimMap = ["acr=Level4"])
@Validated
class PersonController(
    private val personService: PersonService,
) {

    @GetMapping
    fun hentSøker(): PersonMedBarnDto {
        return personService.hentSøker(Fødselsnummer(EksternBrukerUtils.hentFnrFraToken()))
    }

    @GetMapping("med-barn")
    fun hentSøkerMedBarn(): PersonMedBarnDto {
        return personService.hentSøker(Fødselsnummer(EksternBrukerUtils.hentFnrFraToken())) // TODO uten barn
    }
}
