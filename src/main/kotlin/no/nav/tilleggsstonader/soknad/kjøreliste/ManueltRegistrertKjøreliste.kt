package no.nav.tilleggsstonader.soknad.kjøreliste

import java.time.LocalDate

data class ManueltRegistrertKjøreliste(
    val reisedager: List<ManueltRegistrertKjørelisteDag>,
)

data class ManueltRegistrertKjørelisteDag(
    val dato: LocalDate,
    val harKjørt: Boolean,
    val parkeringsutgift: Int?,
)
