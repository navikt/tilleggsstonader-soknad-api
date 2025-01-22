package no.nav.tilleggsstonader.soknad.aktivitet

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.libs.log.SecureLogger.secureLogger
import no.nav.tilleggsstonader.libs.sikkerhet.EksternBrukerUtils
import org.slf4j.LoggerFactory
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
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

    @Deprecated("Erstattes av endepunkt som sender med stønadstype for å kunne variere hvor langt tilbake man henter aktiviteter")
    @GetMapping
    fun hentAktiviteter(): AktiviteterDto {
        val ident = EksternBrukerUtils.hentFnrFraToken()
        return try {
            val aktiviteter = aktivitetService.hentAktiviteter(ident, Stønadstype.BARNETILSYN)
            AktiviteterDto(
                aktiviteter = aktiviteter.gjeldende().mapNotNull { it.tilDto() }.sortedByDescending { it.fom },
                harAktiviteter = aktiviteter.isNotEmpty(),
                suksess = true,
            )
        } catch (e: Exception) {
            logger.warn("Feilet henting av aktiviteter")
            secureLogger.warn("Feiltet henting av aktiviteter for ident=$ident", e)
            AktiviteterDto(emptyList(), harAktiviteter = false, suksess = false)
        }
    }

    @PostMapping
    fun hentAktiviteterV2(@RequestBody request: AktivitetRequest): AktiviteterDto {
        val ident = EksternBrukerUtils.hentFnrFraToken()
        return try {
            val aktiviteter = aktivitetService.hentAktiviteter(ident, request.stønadstype)
            AktiviteterDto(
                aktiviteter = aktiviteter.gjeldende().mapNotNull { it.tilDto() }.sortedByDescending { it.fom },
                harAktiviteter = aktiviteter.isNotEmpty(),
                suksess = true,
            )
        } catch (e: Exception) {
            logger.warn("Feilet henting av aktiviteter")
            secureLogger.warn("Feiltet henting av aktiviteter for ident=$ident", e)
            AktiviteterDto(emptyList(), harAktiviteter = false, suksess = false)
        }
    }
}
