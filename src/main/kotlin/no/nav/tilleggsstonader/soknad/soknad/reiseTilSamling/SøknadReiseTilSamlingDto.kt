package no.nav.tilleggsstonader.soknad.soknad.reiseTilSamling

import no.nav.tilleggsstonader.kontrakter.søknad.DokumentasjonFelt
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFelt
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFlereValgFelt
import no.nav.tilleggsstonader.kontrakter.søknad.JaNei
import no.nav.tilleggsstonader.kontrakter.søknad.SelectFelt
import no.nav.tilleggsstonader.kontrakter.søknad.VerdiFelt
import no.nav.tilleggsstonader.kontrakter.søknad.felles.AnnenAktivitetType
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.AktivitetTypeUtdanning
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.DrivstoffType
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.KanBenytteEgenBil
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.KanIkkeBenytteEgenBilBegrunnelser
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.KanIkkeReiseMedOffentligTransportBegrunnelser
import no.nav.tilleggsstonader.soknad.soknad.HovedytelseDto
import no.nav.tilleggsstonader.soknad.soknad.SøknadMetadataDto

data class SøknadReiseTilSamlingDto(
    val hovedytelse: HovedytelseDto,
    val aktivitet: AktivitetDto,
    val samlinger: List<SamlingDto>,
    val avreiseadresse: AvreiseadresseDto,
    val reisemåte: ReisemåteDto,
    val dokumentasjon: List<DokumentasjonFelt>,
    val søknadMetadata: SøknadMetadataDto,
)

data class TilleggsopplysningerAnnenAktivitetDto(
    val erLærlingEllerLiknende: EnumFelt<JaNei>?,
    val fårDekketReise: EnumFelt<JaNei>?,
    val erUnder25År: EnumFelt<JaNei>?,
    val måBetaleForReiseTilSkole: EnumFelt<JaNei>?,
)

data class AktivitetDto(
    val aktiviteter: EnumFlereValgFelt<String>?,
    val annenAktivitet: EnumFelt<AnnenAktivitetType>?,
    val lønnetAktivitet: EnumFelt<JaNei>?,
    val tilleggsopplysningerAnnenAktivitet: TilleggsopplysningerAnnenAktivitetDto?,
    val annenAktivitetTypeUtdanning: EnumFelt<AktivitetTypeUtdanning>?,
)

data class SamlingDto(
    val fom: VerdiFelt<String>,
    val tom: VerdiFelt<String>,
    val erObligatorisk: EnumFelt<JaNei>,
    val harBruktEkstraReiseDager: EnumFelt<JaNei>,
    val adresse: AdresseDto,
    val antallKilometerEnVei: VerdiFelt<String>,
)

data class AdresseDto(
    val land: SelectFelt<String>?,
    val gateadresse: VerdiFelt<String>?,
    val postnummer: VerdiFelt<String>?,
    val poststed: VerdiFelt<String>?,
)

data class AvreiseadresseDto(
    val skalReiseFraFolkeregistrertAdresse: EnumFelt<JaNei>,
    val adresseDetSkalReisesFra: AdresseDto?,
)

data class ReisemåteDto(
    val kanReiseMedOffentligTransport: EnumFelt<JaNei>,
    val kanIkkeReiseMedOffentligTransportBegrunnelser: EnumFlereValgFelt<KanIkkeReiseMedOffentligTransportBegrunnelser>?,
    val totalUtgifterOffentligTransport: VerdiFelt<String>?,
    val kanBenytteEgenBil: EnumFelt<KanBenytteEgenBil>?,
    val ønskerDekketUtgifterForDrosje: EnumFelt<JaNei>?,
    val barnehageGateadresse: VerdiFelt<String>?,
    val barnehagePostnummer: VerdiFelt<String>?,
    val kanIkkeBenytteEgenBilBegrunnelser: EnumFlereValgFelt<KanIkkeBenytteEgenBilBegrunnelser>?,
    val betalerForReiseSelv: EnumFelt<JaNei>?,
    val harTTKort: EnumFelt<JaNei>?,
    val reiseMedBilUtgifter: ReiseMedBilUtgifterDto?,
)

data class ReiseMedBilUtgifterDto(
    val drivstoffType: EnumFelt<DrivstoffType>,
    val bompenger: VerdiFelt<String>?,
    val ferge: VerdiFelt<String>?,
    val piggdekkavgift: VerdiFelt<String>?,
)
