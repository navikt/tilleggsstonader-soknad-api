package no.nav.tilleggsstonader.soknad.kjøreliste

import no.nav.tilleggsstonader.kontrakter.søknad.DatoFelt
import no.nav.tilleggsstonader.kontrakter.søknad.DokumentasjonFelt
import no.nav.tilleggsstonader.kontrakter.søknad.NumeriskFelt
import no.nav.tilleggsstonader.soknad.soknad.SøknadMetadataDto

data class KjørelisteDto(
    val reisedagerPerUkeAvsnitt: List<UkeMedReisedagerDto>,
    val dokumentasjon: List<DokumentasjonFelt>,
    val søknadMetadata: SøknadMetadataDto,
)

data class UkeMedReisedagerDto(
    val ukeLabel: String,
    val spørsmål: String,
    val reisedager: List<ReisedagDto>,
)

data class ReisedagDto(
    val dato: DatoFelt,
    val harKjørt: Boolean,
    val parkeringsutgift: NumeriskFelt? = null,
)
