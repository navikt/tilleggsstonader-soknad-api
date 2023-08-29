package no.nav.tilleggsstonader.soknad.person.pdl.dto

data class PdlSøkerData(val person: PdlSøker?)

data class PdlSøker(
    val adressebeskyttelse: List<Adressebeskyttelse>,
    val bostedsadresse: List<Bostedsadresse>,
    val forelderBarnRelasjon: List<ForelderBarnRelasjon>,
    val navn: List<Navn>,
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

data class Navn(val fornavn: String, val mellomnavn: String?, val etternavn: String)
