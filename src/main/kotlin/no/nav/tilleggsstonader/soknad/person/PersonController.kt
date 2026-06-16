package no.nav.tilleggsstonader.soknad.person

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tilleggsstonader.kontrakter.felles.IdentStønadstype
import no.nav.tilleggsstonader.kontrakter.felles.Skjematype
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.kontrakter.felles.tilStønadstyper
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
    fun hentSøker(): PersonMedBarnDto = personService.hentSøker(Fødselsnummer(EksternBrukerUtils.hentFnrFraToken()), medBarn = false)

    @GetMapping("med-barn")
    fun hentSøkerMedBarn(): PersonMedBarnDto = personService.hentSøker(Fødselsnummer(EksternBrukerUtils.hentFnrFraToken()), medBarn = true)

    @GetMapping("har-behandling")
    fun harBehandlingUnderArbeid(
        @RequestParam("stonadstype") stønadstype: Stønadstype?,
        @RequestParam("skjematype") skjematype: Skjematype?,
    ): Boolean {
        val fødselsnummer = EksternBrukerUtils.hentFnrFraToken()
        // TODO: Trenger midlertidig å støtte både stønadstype og skjematype. Når frontend har oppdatert til å sende stønadstype, kan koden forenkles og skjematype gjøres obligatorisk.
        return saksbehandlingClient.harBehandlingUnderArbeid(
            IdentStønadstype(
                fødselsnummer,
                stønadstype ?: skjematype!!.tilStønadstyper().first(),
            ),
        )
    }
}
