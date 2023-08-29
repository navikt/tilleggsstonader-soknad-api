package no.nav.tilleggsstonader.soknad.person.pdl.dto

data class PdlResponse<T>(
    val data: T,
    val errors: List<PdlError>?,
    val extensions: PdlExtensions?,
) {

    fun harFeil(): Boolean {
        return errors != null && errors.isNotEmpty()
    }

    fun harAdvarsel(): Boolean {
        return !extensions?.warnings.isNullOrEmpty()
    }

    fun errorMessages(): String {
        return errors?.joinToString { it -> it.message } ?: ""
    }
}

data class PdlBolkResponse<T>(val data: PersonBolk<T>?, val errors: List<PdlError>?, val extensions: PdlExtensions?) {

    fun errorMessages(): String {
        return errors?.joinToString { it -> it.message } ?: ""
    }

    fun harAdvarsel(): Boolean {
        return !extensions?.warnings.isNullOrEmpty()
    }
}

data class PdlError(
    val message: String,
    val extensions: PdlErrorExtensions?,
)

data class PdlErrorExtensions(val code: String?) {

    fun notFound() = code == "not_found"
}

data class PdlExtensions(val warnings: List<PdlWarning>?)
data class PdlWarning(val details: Any?, val id: String?, val message: String?, val query: String?)

data class PersonDataBolk<T>(val ident: String, val code: String, val person: T?)
data class PersonBolk<T>(val personBolk: List<PersonDataBolk<T>>)
