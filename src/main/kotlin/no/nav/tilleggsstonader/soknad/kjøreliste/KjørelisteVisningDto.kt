package no.nav.tilleggsstonader.soknad.kjøreliste

import java.time.LocalDate

data class KjørelisteVisningDto(
    val reisedager: List<ReisedagVisningDto>,
)

data class ReisedagVisningDto(
    val dato: LocalDate,
    val harKjørt: Boolean,
    val parkeringsutgift: Int?,
)
