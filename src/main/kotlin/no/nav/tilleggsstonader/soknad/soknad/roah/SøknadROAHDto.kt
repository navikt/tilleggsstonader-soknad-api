package no.nav.tilleggsstonader.soknad.soknad.roah

import no.nav.tilleggsstonader.kontrakter.søknad.DokumentasjonFelt
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFelt
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFlereValgFelt
import no.nav.tilleggsstonader.kontrakter.søknad.JaNei
import no.nav.tilleggsstonader.kontrakter.søknad.VerdiFelt
import no.nav.tilleggsstonader.kontrakter.søknad.felles.AnnenAktivitetType
import no.nav.tilleggsstonader.kontrakter.søknad.roah.ROAHTypeReise
import no.nav.tilleggsstonader.soknad.soknad.HovedytelseDto
import no.nav.tilleggsstonader.soknad.soknad.SøknadMetadataDto
import no.nav.tilleggsstonader.soknad.soknad.reiseTilSamling.AdresseDto
import no.nav.tilleggsstonader.soknad.soknad.reiseTilSamling.ReisemåteDto

data class SøknadROAHDto(
    val hovedytelse: HovedytelseDto,
    val aktivitet: AktivitetDto,
    val adresser: AdresserDto,
    val barnOgSærligeBehov: BarnOgSærligeBehovDto,
    val reiser: List<ReiseInfoDto>,
    val dokumentasjon: List<DokumentasjonFelt>,
    val søknadMetadata: SøknadMetadataDto,
)

data class AktivitetDto(
    val aktiviteter: EnumFlereValgFelt<String>?,
    val annenAktivitet: EnumFelt<AnnenAktivitetType>?,
    val lønnetAktivitet: EnumFelt<JaNei>?,
)

data class AdresserDto(
    val måBoBorteHjemmefra: EnumFelt<JaNei>?,
    val periodeMidlertidigBosted: PeriodeMidlertidigBostedDto?,
    val adresseOriginaltBosted: AdresseOriginaltBostedDto?,
    val adresseMidlertidigBosted: AdresseDto?,
)

data class PeriodeMidlertidigBostedDto(
    val fomFlyttedato: VerdiFelt<String>?,
    val tomFlyttedato: VerdiFelt<String>?,
)

data class AdresseOriginaltBostedDto(
    val skalReiseFraFolkeregistrertAdresse: EnumFelt<JaNei>?,
    val adresseOriginaltBosted: AdresseDto?,
)

data class BarnOgSærligeBehovDto(
    val harBarnUnder18SomHarFlyttetMed: EnumFelt<JaNei>?,
    val hvilkeBarnFlytterMed: EnumFlereValgFelt<String>?,
    val harBarnHjemmeUnder4Klasse: EnumFelt<JaNei>?,
    val harSærligeBehovForFlereHjemreiser: EnumFelt<JaNei>?,
)

data class ReiseInfoDto(
    val typeReise: EnumFelt<ROAHTypeReise>?,
    val fom: VerdiFelt<String>?,
    val tom: VerdiFelt<String>?,
    val reisemåte: ReisemåteDto?,
)
