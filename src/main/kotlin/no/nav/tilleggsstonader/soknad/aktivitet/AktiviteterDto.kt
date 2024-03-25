package no.nav.tilleggsstonader.soknad.aktivitet

import no.nav.tilleggsstonader.kontrakter.aktivitet.AktivitetArenaDto

data class AktiviteterDto(
    val aktiviteter: List<AktivitetArenaDto>,
    val sukkess: Boolean,
)
