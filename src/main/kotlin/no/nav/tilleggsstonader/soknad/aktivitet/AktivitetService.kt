package no.nav.tilleggsstonader.soknad.aktivitet

import no.nav.tilleggsstonader.kontrakter.aktivitet.AktivitetArenaDto
import no.nav.tilleggsstonader.kontrakter.aktivitet.GruppeAktivitet
import no.nav.tilleggsstonader.kontrakter.aktivitet.TypeAktivitet
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
    fun hentAktiviteter(ident: String): List<AktivitetArenaDto> {
        val fom = osloDateNow().minusMonths(3)
        val tom = osloDateNow().plusMonths(3)
        return aktivitetClient.hentAktiviteter(ident, fom, tom)
            .filter(::skalVises)
    }

    /**
     * Filtrer vekk de av andre gruppetyper som ikke er interessante å vise i hverken saksbehandling eller søknad
     * Tar med alle som er markert som [erStønadsberettiget]
     * Eks filtrerer denne bort [INDOP] som er av gruppetype [SAK]
     */
    private fun skalVises(it: AktivitetArenaDto) = try {
        // Ikke alle aktiviteter har fått flagg "stønadsberettiget" i Arena selv om de skulle hatt det, så vi trenger en ekstra sjekk på gruppe
        // Det er alltid gruppe=TILTAK når erStønadsberettiget = true, men ikke alle tiltak er stønadsberettiget
        it.erStønadsberettiget == true || TypeAktivitet.valueOf(it.type).gruppe == GruppeAktivitet.TLTAK
    } catch (e: Exception) {
        logger.error("TypeAktivitet mangler mapping, se secure logs for detaljer.")
        secureLogger.error("TypeAktivitet=${it.type} mangler mapping. Vennligst oppdater TypeAktivitet med ny type.")
        false
    }
}
