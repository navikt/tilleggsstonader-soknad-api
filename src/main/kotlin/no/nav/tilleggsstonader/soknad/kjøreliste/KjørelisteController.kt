package no.nav.tilleggsstonader.soknad.kjøreliste

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tilleggsstonader.libs.sikkerhet.EksternBrukerUtils
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/kjorelister")
@ProtectedWithClaims(issuer = EksternBrukerUtils.ISSUER_TOKENX, claimMap = ["acr=Level4"])
@Validated
class KjørelisteController {
    @GetMapping("alle-rammevedtak")
    fun hentAlleRammevedtak(): List<RammevedtakDto> = rammevedtakDtoMock

    @GetMapping("rammevedtak/{rammevedtakId}")
    fun hentRammevedtakDetaljer(
        @PathVariable rammevedtakId: String,
    ): RammevedtakDto? = rammevedtakDtoMock.find { rammevedtak -> rammevedtak.id == rammevedtakId }
}
