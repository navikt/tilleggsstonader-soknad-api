package no.nav.tilleggsstonader.soknad.person.dto

import java.time.LocalDate

data class PersonMedBarnDto(
    val fornavn: String,
    val alder: Int,
    val visningsnavn: String,
    val adresse: String,
    val barn: List<Barn>,
)

data class Barn(
    val ident: String,
    val fornavn: String,
    val visningsnavn: String,
    val fødselsdato: LocalDate,
    val alder: Int,
)
