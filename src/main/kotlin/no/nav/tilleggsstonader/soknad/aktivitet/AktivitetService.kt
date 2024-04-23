package no.nav.tilleggsstonader.soknad.aktivitet

import no.nav.tilleggsstonader.kontrakter.aktivitet.AktivitetArenaDto
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class AktivitetService(
    private val aktivitetClient: AktivitetClient,
) {

    @Cacheable("aktivitet", cacheManager = "aktivitetCache")
    fun hentAktiviteter(ident: String): List<AktivitetArenaDto> {
        val fom = LocalDate.now().minusMonths(3)
        val tom = LocalDate.now().plusMonths(3)
        return aktivitetClient.hentAKtiviteter(ident, fom, tom)
    }
}
