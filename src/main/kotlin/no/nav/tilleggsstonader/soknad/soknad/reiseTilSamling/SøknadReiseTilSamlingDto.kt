package no.nav.tilleggsstonader.soknad.soknad.reiseTilSamling

import no.nav.tilleggsstonader.kontrakter.søknad.DokumentasjonFelt
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFelt
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFlereValgFelt
import no.nav.tilleggsstonader.kontrakter.søknad.JaNei
import no.nav.tilleggsstonader.kontrakter.søknad.SelectFelt
import no.nav.tilleggsstonader.kontrakter.søknad.VerdiFelt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.AnnenAktivitetType
import no.nav.tilleggsstonader.soknad.soknad.HovedytelseDto
import no.nav.tilleggsstonader.soknad.soknad.SøknadMetadataDto

data class SøknadReiseTilSamlingDto(
    val hovedytelse: HovedytelseDto,
    val aktivitet: AktivitetDto,
    val samlinger: List<SamlingDto>,
    val reiseavstand: ReiseavstandDto,
    val reisemåte: ReisemåteDto,
    val dokumentasjon: List<DokumentasjonFelt>,
    val søknadMetadata: SøknadMetadataDto,
)

data class AktivitetDto(
    val aktiviteter: EnumFlereValgFelt<String>?,
    val annenAktivitet: EnumFelt<AnnenAktivitetType>?,
    val lønnetAktivitet: EnumFelt<JaNei>?,
)

data class SamlingDto(
    val fom: VerdiFelt<String>?,
    val tom: VerdiFelt<String>?,
)

data class AdresseDto(
    val land: SelectFelt<String>?,
    val gateadresse: VerdiFelt<String>?,
    val postnummer: VerdiFelt<String>?,
    val poststed: VerdiFelt<String>?,
)

data class ReiseavstandDto(
    val antallKilometerEnVei: VerdiFelt<String>,
    val reiseFraFolkeregistrertAdr: EnumFelt<JaNei>,
    val adresseDetSkalReisesFra: AdresseDto,
    val aktivitetsadresse: AdresseDto,
)

data class ReisemåteDto(
    val kanReiseKollektivt: EnumFelt<JaNei>,
    val totalutgifterKollektivt: VerdiFelt<String>?,
    val kanBenytteEgenBil: EnumFelt<JaNei>?,
    val kanBenytteDrosje: EnumFelt<JaNei>?,
)
