package no.nav.tilleggsstonader.soknad.aktivitet

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tilleggsstonader.kontrakter.aktivitet.AktivitetArenaDto
import no.nav.tilleggsstonader.libs.sikkerhet.EksternBrukerUtils
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/aktivitet")
@ProtectedWithClaims(issuer = EksternBrukerUtils.ISSUER_TOKENX, claimMap = ["acr=Level4"])
@Validated
class AktivitetController(
    private val aktivitetService: AktivitetService,
) {

    @GetMapping
    fun hentAktiviteter(): List<AktivitetArenaDto> {
        return aktivitetService.hentAktiviteter(EksternBrukerUtils.hentFnrFraToken())
    }
}
