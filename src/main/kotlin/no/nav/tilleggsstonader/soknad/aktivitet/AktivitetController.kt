package no.nav.tilleggsstonader.soknad.aktivitet

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tilleggsstonader.libs.log.SecureLogger.secureLogger
import no.nav.tilleggsstonader.libs.sikkerhet.EksternBrukerUtils
import org.slf4j.LoggerFactory
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

    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun hentAktiviteter(): AktiviteterDto {
        val ident = EksternBrukerUtils.hentFnrFraToken()
        return try {
            val aktiviteter = aktivitetService.hentAktiviteter(ident)
            AktiviteterDto(
                aktiviteter = aktiviteter.mapNotNull { it.tilDto() },
                harAktiviteter = aktiviteter.isNotEmpty(),
                suksess = true,
            )
        } catch (e: Exception) {
            logger.error("Feilet henting av aktiviteter")
            secureLogger.error("Feiltet henting av aktiviteter for ident=$ident", e)
            AktiviteterDto(emptyList(), harAktiviteter = false, suksess = false)
        }
    }
}
