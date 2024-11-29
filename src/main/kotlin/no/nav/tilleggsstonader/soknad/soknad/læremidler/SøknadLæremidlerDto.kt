package no.nav.tilleggsstonader.soknad.soknad.læremidler

import no.nav.tilleggsstonader.kontrakter.søknad.DokumentasjonFelt
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFelt
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFlereValgFelt
import no.nav.tilleggsstonader.kontrakter.søknad.JaNei
import no.nav.tilleggsstonader.kontrakter.søknad.læremidler.AnnenUtdanningType
import no.nav.tilleggsstonader.soknad.soknad.HovedytelseDto

data class SøknadLæremidlerDto(
    val hovedytelse: HovedytelseDto,
    val utdanning: Utdanning,
    val dokumentasjon: List<DokumentasjonFelt>,
)

data class Utdanning(
    val aktiviteter: EnumFlereValgFelt<String>?,
    val annenUtdanning: EnumFelt<AnnenUtdanningType>?,
    val erLærlingEllerLiknende: EnumFelt<JaNei>?,
    val harFunksjonsnedsettelse: EnumFelt<JaNei>,
)
