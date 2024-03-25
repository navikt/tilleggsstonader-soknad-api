package no.nav.tilleggsstonader.soknad.kodeverk

import no.nav.tilleggsstonader.kontrakter.kodeverk.KodeverkDto
import no.nav.tilleggsstonader.kontrakter.kodeverk.KodeverkSpråk
import no.nav.tilleggsstonader.kontrakter.kodeverk.hentGjeldende
import no.nav.tilleggsstonader.kontrakter.kodeverk.mapTerm
import no.nav.tilleggsstonader.soknad.kodeverk.KodeverkUtil.UGYLDIGE_LANDKODER
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

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

    fun hentLandkoder(språk: KodeverkSpråk) = hentLandkoderForSpråk(språk)

    private fun hentLandkoderForSpråk(språk: KodeverkSpråk): Map<String, String> {
        require(språk == KodeverkSpråk.BOKMÅL) {
            "Har ikke støtte i klienten for å håndtere annet språk enn ${KodeverkSpråk.BOKMÅL}"
        }
        return cachedKodeverkService.hentLandkoder()
            .mapTerm(språk)
            .filterKeys { it !in UGYLDIGE_LANDKODER }
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
    @Cacheable("poststed", sync = true)
    fun hentPoststed(): KodeverkDto = kodeverkClient.hentPostnummer()

    @Cacheable("landkoder", sync = true)
    fun hentLandkoder(): KodeverkDto = kodeverkClient.hentLandkoder()
}
