package no.nav.tilleggsstonader.soknad.kjøreliste

import no.nav.tilleggsstonader.kontrakter.søknad.UkeMedReisedager
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class Uke(
    val mandag: LocalDate,
    val søndag: LocalDate,
) {
    constructor(dato: LocalDate) : this(
        mandag = dato.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
        søndag = dato.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)),
    )
}

fun UkeMedReisedager.tilUke(): Uke = Uke(reisedager.first().dato.verdi)

fun UkeMedReisedagerDto.tilUke(): Uke = Uke(reisedager.first().dato.verdi)
