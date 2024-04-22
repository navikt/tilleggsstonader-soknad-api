package no.nav.tilleggsstonader.soknad.soknad

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tilleggsstonader.libs.sikkerhet.EksternBrukerUtils
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.SøknadBarnetilsynDto
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("api/soknad")
@ProtectedWithClaims(issuer = EksternBrukerUtils.ISSUER_TOKENX, claimMap = ["acr=Level4"])
@Validated
class SøknadController(
    private val søknadService: SøknadService,
) {

    // todo fjerne "barnetilsyn" når pass-barn er tatt i bruk
    @PostMapping("barnetilsyn", "pass-av-barn")
    fun sendInn(@RequestBody søknad: SøknadBarnetilsynDto): Kvittering {
        val mottattTidspunkt = LocalDateTime.now()
        søknadService.lagreSøknad(
            ident = EksternBrukerUtils.hentFnrFraToken(),
            mottattTidspunkt = mottattTidspunkt,
            søknad = søknad,
        )
        return Kvittering(mottattTidspunkt = mottattTidspunkt)
    }
}
