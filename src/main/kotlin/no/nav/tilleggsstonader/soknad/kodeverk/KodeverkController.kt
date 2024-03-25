package no.nav.tilleggsstonader.soknad.kodeverk

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tilleggsstonader.kontrakter.kodeverk.KodeverkSpråk
import no.nav.tilleggsstonader.libs.sikkerhet.EksternBrukerUtils
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/kodeverk")
@ProtectedWithClaims(issuer = EksternBrukerUtils.ISSUER_TOKENX, claimMap = ["acr=Level4"])
@Validated
class KodeverkController(
    private val kodeverkService: KodeverkService,
) {

    @GetMapping("landkoder/nb")
    fun hentLandkoder(): Map<String, String> {
        return kodeverkService.hentLandkoder(KodeverkSpråk.BOKMÅL) // Virker kanskje ikke som at det finnes på annet språk
    }
}
