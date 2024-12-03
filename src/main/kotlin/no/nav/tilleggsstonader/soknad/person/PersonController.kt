package no.nav.tilleggsstonader.soknad.person

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tilleggsstonader.kontrakter.felles.IdentStønadstype
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.libs.sikkerhet.EksternBrukerUtils
import no.nav.tilleggsstonader.libs.utils.fnr.Fødselsnummer
import no.nav.tilleggsstonader.soknad.person.dto.PersonMedBarnDto
import no.nav.tilleggsstonader.soknad.sak.SaksbehandlingClient
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/person")
@ProtectedWithClaims(issuer = EksternBrukerUtils.ISSUER_TOKENX, claimMap = ["acr=Level4"])
@Validated
class PersonController(
    private val personService: PersonService,
    private val saksbehandlingClient: SaksbehandlingClient,
) {

    @GetMapping
    fun hentSøker(): PersonMedBarnDto {
        return personService.hentSøker(Fødselsnummer(EksternBrukerUtils.hentFnrFraToken()), medBarn = false)
    }

    @GetMapping("med-barn")
    fun hentSøkerMedBarn(): PersonMedBarnDto {
        return personService.hentSøker(Fødselsnummer(EksternBrukerUtils.hentFnrFraToken()), medBarn = true)
    }

    @GetMapping("behandlingStatus")
    fun hentBehandlingsstatus(@RequestParam("stonadstype") stonadstype: Stønadstype): Boolean {
        val fødselsnummer = EksternBrukerUtils.hentFnrFraToken()
        return saksbehandlingClient.hentBehandlingStatus(IdentStønadstype(fødselsnummer, stonadstype))
    }
}
