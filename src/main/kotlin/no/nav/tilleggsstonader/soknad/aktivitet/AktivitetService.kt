package no.nav.tilleggsstonader.soknad.aktivitet

import no.nav.tilleggsstonader.kontrakter.aktivitet.AktivitetArenaDto
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.libs.log.SecureLogger.secureLogger
import no.nav.tilleggsstonader.libs.utils.osloDateNow
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class AktivitetService(
    private val aktivitetClient: AktivitetClient,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Cacheable("aktivitet", cacheManager = "aktivitetCache")
    fun hentAktiviteter(
        ident: String,
        stønadstype: Stønadstype,
    ): List<AktivitetArenaDto> {
        val fom = osloDateNow().minusMonths(stønadstype.antallMånederBakITiden())
        val tom = osloDateNow().plusMonths(3)
        return aktivitetClient
            .hentAktiviteter(ident, fom, tom)
            .filter(::skalVises)
    }

    /**
     * Filtrer vekk de av andre gruppetyper som ikke er interessante å vise i hverken saksbehandling eller søknad
     * Tar med alle som er markert som [erStønadsberettiget]
     * Eks filtrerer denne bort [INDOP] som er av gruppetype [SAK]
     */
    private fun skalVises(it: AktivitetArenaDto) =
        try {
            it.erStønadsberettiget == true
        } catch (e: Exception) {
            logger.error("TypeAktivitet mangler mapping, se secure logs for detaljer.")
            secureLogger.error("TypeAktivitet=${it.type} mangler mapping. Vennligst oppdater TypeAktivitet med ny type.")
            false
        }

    private fun Stønadstype.antallMånederBakITiden(): Long =
        when (this) {
            Stønadstype.BARNETILSYN -> 3
            Stønadstype.LÆREMIDLER -> 6
        }
}
