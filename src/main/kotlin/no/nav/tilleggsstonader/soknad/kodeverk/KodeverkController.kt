package no.nav.tilleggsstonader.soknad.kodeverk

import no.nav.security.token.support.core.api.Unprotected
import no.nav.tilleggsstonader.kontrakter.kodeverk.KodeverkSpråk
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/kodeverk")
@Validated
class KodeverkController(
    private val kodeverkService: KodeverkService,
) {

    @Unprotected
    @GetMapping("landkoder/nb")
    fun hentLandkoder(): Map<String, String> {
        return kodeverkService.hentLandkoder(KodeverkSpråk.BOKMÅL) // Virker kanskje ikke som at det finnes på annet språk
    }
}
