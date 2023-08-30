package no.nav.tilleggsstonader.soknad.soknad

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tilleggsstonader.libs.sikkerhet.EksternBrukerUtils
import no.nav.tilleggsstonader.soknad.infrastruktur.config.SecureLogger.secureLogger
import no.nav.tilleggsstonader.soknad.soknad.dto.Kvittering
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
class SøknadController {

    @PostMapping("barnetilsyn")
    fun sendInn(@RequestBody søknad: Map<String, Any>): Kvittering {
        /*if (!EksternBrukerUtils.personIdentErLikInnloggetBruker()) {
            throw ApiFeil("Fnr fra token matcher ikke fnr på søknaden", HttpStatus.FORBIDDEN)
        }*/
        val mottattTidspunkt = LocalDateTime.now()
        secureLogger.info("Mottatt søknad fra ${EksternBrukerUtils.hentFnrFraToken()}") // slett denne senere
        // søknadService.sendInn(søknad, innsendingMottatt)
        return Kvittering(mottattTidspunkt = mottattTidspunkt)
    }
}
