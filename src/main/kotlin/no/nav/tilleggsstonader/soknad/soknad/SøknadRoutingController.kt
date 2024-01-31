package no.nav.tilleggsstonader.soknad.soknad

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tilleggsstonader.kontrakter.felles.IdentStønadstype
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.libs.sikkerhet.EksternBrukerUtils
import no.nav.tilleggsstonader.soknad.sak.SaksbehandlingClient
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/soknad-routing")
@ProtectedWithClaims(issuer = EksternBrukerUtils.ISSUER_TOKENX, claimMap = ["acr=Level4"])
@Validated
class SøknadRoutingController(
    private val saksbehandlingClient: SaksbehandlingClient,
) {

    @PostMapping
    fun sjekkRoutingForPerson(@RequestBody request: RoutingRequest): RoutingResponse {
        val skalRoutesTilNyLøsning = saksbehandlingClient.skalRoutesTilNyLøsning(
            IdentStønadstype(
                ident = EksternBrukerUtils.hentFnrFraToken(),
                stønadstype = request.stønadstype,
            ),
        )
        return RoutingResponse(skalBehandlesINyLøsning = skalRoutesTilNyLøsning)
    }
}

data class RoutingRequest(
    val stønadstype: Stønadstype,
)

data class RoutingResponse(
    val skalBehandlesINyLøsning: Boolean,
)
