package no.nav.tilleggsstonader.soknad.kjøreliste

data class Reisedag(
    val harReist: Boolean,
    val parkeringsutgift: Int? = null,
)

data class Kjøreliste(
    val reisedager: Map<String, Reisedag>,
)
