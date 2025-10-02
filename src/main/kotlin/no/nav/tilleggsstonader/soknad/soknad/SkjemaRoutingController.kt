package no.nav.tilleggsstonader.soknad.soknad

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tilleggsstonader.kontrakter.felles.IdentSkjematype
import no.nav.tilleggsstonader.kontrakter.felles.Skjematype
import no.nav.tilleggsstonader.libs.sikkerhet.EksternBrukerUtils
import no.nav.tilleggsstonader.soknad.sak.SaksbehandlingClient
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/skjema-routing")
@ProtectedWithClaims(issuer = EksternBrukerUtils.ISSUER_TOKENX, claimMap = ["acr=Level4"])
@Validated
class SkjemaRoutingController(
    private val saksbehandlingClient: SaksbehandlingClient,
) {
    @PostMapping
    fun skalBrukerRoutesTilNyLøsning(
        @RequestBody request: SkjemaRoutingRequest,
    ): SkjemaRoutingResponse {
        val skalRoutesTilNyLøsning =
            saksbehandlingClient.skalRoutesTilNyLøsning(
                IdentSkjematype(
                    ident = EksternBrukerUtils.hentFnrFraToken(),
                    skjematype = request.skjematype,
                ),
            )
        return SkjemaRoutingResponse(skalRoutesTilNyLøsning)
    }
}

data class SkjemaRoutingResponse(
    val skalBehandlesINyLøsning: Boolean,
)

data class SkjemaRoutingRequest(
    val skjematype: Skjematype,
)
