package no.nav.tilleggsstonader.soknad.person.pdl

open class PdlRequestException(
    melding: String? = null,
) : Exception(melding)

class PdlNotFoundException : PdlRequestException()
