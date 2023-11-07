package no.nav.tilleggsstonader.soknad.person.pdl.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class PdlSøkerData(val person: PdlSøker?)

data class PdlSøker(
    val adressebeskyttelse: List<Adressebeskyttelse>,
    val bostedsadresse: List<Bostedsadresse>,
    val forelderBarnRelasjon: List<ForelderBarnRelasjon>,
    val navn: List<Navn>,
)

data class PdlBarn(
    val adressebeskyttelse: List<Adressebeskyttelse>,
    val navn: List<Navn>,
    @JsonProperty("foedsel")
    val fødsel: List<Fødsel>,
    @JsonProperty("doedsfall")
    val dødsfall: List<Dødsfall>,
)

data class Fødsel(
    @JsonProperty("foedselsaar")
    val fødselsår: Int?,
    @JsonProperty("foedselsdato")
    val fødselsdato: LocalDate?,
)

data class Dødsfall(
    @JsonProperty("doedsdato") val dødsdato: LocalDate?,
)

data class Adressebeskyttelse(val gradering: AdressebeskyttelseGradering)

enum class AdressebeskyttelseGradering {

    STRENGT_FORTROLIG,
    STRENGT_FORTROLIG_UTLAND,
    FORTROLIG,
    UGRADERT,
}

data class Bostedsadresse(val vegadresse: Vegadresse?, val matrikkeladresse: Matrikkeladresse?)

data class Vegadresse(
    val husnummer: String?,
    val husbokstav: String?,
    val bruksenhetsnummer: String?,
    val adressenavn: String?,
    val postnummer: String?,
)

data class Matrikkeladresse(val tilleggsnavn: String?, val postnummer: String?)

data class ForelderBarnRelasjon(
    val relatertPersonsIdent: String?,
    val relatertPersonsRolle: Familierelasjonsrolle,
)

enum class Familierelasjonsrolle {
    BARN,
    MOR,
    FAR,
    MEDMOR,
}

data class Navn(val fornavn: String, val mellomnavn: String?, val etternavn: String) {

    fun visningsnavn(): String {
        return if (mellomnavn.isNullOrEmpty()) {
            "$fornavn $etternavn"
        } else {
            "$fornavn $mellomnavn $etternavn"
        }
    }
}
