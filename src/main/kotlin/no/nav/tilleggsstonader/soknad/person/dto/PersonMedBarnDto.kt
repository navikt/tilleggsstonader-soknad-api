package no.nav.tilleggsstonader.soknad.person.dto

import java.time.LocalDate

data class PersonMedBarnDto(
    val fornavn: String,
    val alder: Int,
    val visningsnavn: String,
    val adresse: String,
    val strukturertAdresse: Adresse?,
    val barn: List<Barn>,
)

data class Adresse(
    val land: String? = null,
    val gateadresse: String? = null,
    val postnummer: String? = null,
    val poststed: String? = null,
)

data class Barn(
    val ident: String,
    val fornavn: String,
    val visningsnavn: String,
    val fødselsdato: LocalDate,
    val alder: Int,
)
