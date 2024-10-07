package no.nav.tilleggsstonader.soknad.soknad.laeremidler

import no.nav.tilleggsstonader.kontrakter.søknad.DokumentasjonFelt
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFelt
import no.nav.tilleggsstonader.kontrakter.søknad.JaNei
import no.nav.tilleggsstonader.kontrakter.søknad.læremidler.AnnenUtdanningType
import no.nav.tilleggsstonader.soknad.soknad.HovedytelseDto

data class SøknadLæremidlerDto(
    val hovedytelse: HovedytelseDto,
    val utdanning: Utdanning,
    val dokumentasjon: List<DokumentasjonFelt>,
)

data class Utdanning(
    val annenUtdanning: EnumFelt<AnnenUtdanningType>?,
    val mottarUtstyrsstipend: EnumFelt<JaNei>?,
    val harFunksjonsnedsettelse: EnumFelt<JaNei>,
)
