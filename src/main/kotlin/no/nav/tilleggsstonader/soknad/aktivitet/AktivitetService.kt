package no.nav.tilleggsstonader.soknad.aktivitet

import no.nav.tilleggsstonader.kontrakter.aktivitet.AktivitetArenaDto
import no.nav.tilleggsstonader.libs.utils.osloDateNow
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class AktivitetService(
    private val aktivitetClient: AktivitetClient,
) {

    @Cacheable("aktivitet", cacheManager = "aktivitetCache")
    fun hentAktiviteter(ident: String): List<AktivitetArenaDto> {
        val fom = osloDateNow().minusMonths(3)
        val tom = osloDateNow().plusMonths(3)
        return aktivitetClient.hentAKtiviteter(ident, fom, tom)
    }
}
