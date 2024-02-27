package no.nav.tilleggsstonader.soknad.kodeverk

import no.nav.tilleggsstonader.kontrakter.kodeverk.KodeverkDto
import no.nav.tilleggsstonader.kontrakter.kodeverk.hentGjeldende
import no.nav.tilleggsstonader.libs.log.mdc.MDCConstants
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.UUID

/**
 * Henter cachade verdier fra kodeverk. Kodeverk inneholder mapping av div verdier til norsk tekst av verdiet.
 * Eks inneholder kodeverk mapping fra postnummer 0010 til Oslo
 */
@Service
class KodeverkService(
    private val cachedKodeverkService: CachedKodeverkService,
) {

    private val logger = LoggerFactory.getLogger(javaClass)
    fun hentPoststed(postnummer: String?) = hentKodeverdi("poststed", postnummer) {
        cachedKodeverkService.hentPoststed().hentGjeldende(it)
    }

    private fun hentKodeverdi(type: String, kode: String?, hentKodeverdiFunction: Function1<String, String?>): String {
        return try {
            kode?.let(hentKodeverdiFunction) ?: kode ?: ""
        } catch (e: Exception) {
            // Ikke la feil kodeverk stoppe henting av data
            logger.error("Feilet henting av $type til $kode message=${e.message} cause=${e.cause?.message}")
            ""
        }
    }
}

@Service
@CacheConfig(cacheManager = "kodeverkCache")
class CachedKodeverkService(
    private val kodeverkClient: KodeverkClient,
) {
    @Cacheable("kodeverk_postestedMedHistorikk", sync = true)
    fun hentPoststed(): KodeverkDto = kodeverkClient.hentPostnummer()
}
