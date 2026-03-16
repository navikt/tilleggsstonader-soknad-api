package no.nav.tilleggsstonader.soknad.kjøreliste

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tilleggsstonader.kontrakter.søknad.RammevedtakDto
import no.nav.tilleggsstonader.libs.sikkerhet.EksternBrukerUtils
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/kjorelister")
@ProtectedWithClaims(issuer = EksternBrukerUtils.ISSUER_TOKENX, claimMap = ["acr=Level4"])
@Validated
class KjørelisteController(
    private val kjørelisteService: KjørelisteService,
) {
    @GetMapping("/alle-rammevedtak")
    fun hentAlleRammevedtak(): List<RammevedtakDto> = kjørelisteService.hentAlleRammevedtakForInnloggetBruker()

    @GetMapping("/rammevedtak/{reiseId}")
    fun hentRammevedtak(
        @PathVariable reiseId: String,
    ): RammevedtakDto = kjørelisteService.hentRammevedtakForInnloggetBruker(reiseId)

    @GetMapping("/{reiseId}")
    fun hentKjørelister(
        @PathVariable reiseId: String,
    ): KjørelisteDto? = kjørelisteService.hentKjørelisterForReise(reiseId)

    @PostMapping
    fun mottaKjørelister(
        @RequestBody kjørelisteDto: KjørelisteDto,
    ): KjørelisteResponse = kjørelisteService.mottaKjøreliste(kjørelisteDto)
}
