package no.nav.tilleggsstonader.soknad.person.pdl.dto

data class PdlPersonRequestVariables(
    var ident: String,
)

data class PdlPersonBolkRequestVariables(
    var identer: List<String>,
)

data class PdlPersonRequest(
    val variables: PdlPersonRequestVariables,
    val query: String,
)

data class PdlPersonBolkRequest(
    val variables: PdlPersonBolkRequestVariables,
    val query: String,
)
