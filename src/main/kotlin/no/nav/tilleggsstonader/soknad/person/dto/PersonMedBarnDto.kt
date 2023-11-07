package no.nav.tilleggsstonader.soknad.person.dto

import java.time.LocalDate

data class PersonMedBarnDto(
    val navn: String,
    val adresse: Adresse?,
    val telefonnr: String,
    val epost: String,
    val kontonr: String,
    val barn: List<Barn>,
)

data class Adresse(
    val adresse: String,
    val postnummer: String,
    val poststed: String,
)

data class Barn(
    val ident: String,
    val navn: String,
    val fødselsdato: LocalDate,
    val alder: Int,
)
