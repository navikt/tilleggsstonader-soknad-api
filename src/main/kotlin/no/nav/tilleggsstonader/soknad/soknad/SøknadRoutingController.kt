package no.nav.tilleggsstonader.soknad.soknad

import no.nav.security.token.support.core.api.ProtectedWithClaims
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
    fun sjekkRoutingForPerson(
        @RequestBody request: RoutingRequest,
    ): RoutingResponse {
        val skalRoutesTilNyLøsning =
            saksbehandlingClient.skalRoutesTilNyLøsning(
                RoutingRequest(
                    ident = EksternBrukerUtils.hentFnrFraToken(),
                    søknadsType = request.søknadsType,
                ),
            )
        return RoutingResponse(skalBehandlesINyLøsning = skalRoutesTilNyLøsning)
    }
}

data class RoutingRequest(
    val ident: String,
    val søknadsType: SøknadsType,
)

data class RoutingResponse(
    val skalBehandlesINyLøsning: Boolean,
)

enum class SøknadsType {
    BARNETILSYN,
    LÆREMIDLER,
    BOUTGIFTER,
    DAGLIG_REISE,
}
