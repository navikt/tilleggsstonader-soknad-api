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
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.Transportmiddel
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.ÅrsakKanIkkeBenytteEgenBil
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.ÅrsakKanIkkeBenytteOffentligTransport
import no.nav.tilleggsstonader.soknad.soknad.HovedytelseDto
import no.nav.tilleggsstonader.soknad.soknad.SøknadMetadataDto

data class SøknadReiseTilSamlingDto(
    val hovedytelse: HovedytelseDto,
    val aktivitet: AktivitetDto,
    val samlinger: List<SamlingDto>,
    val avreiseadresse: AvreiseadresseDto,
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
    val adresse: AdresseDto,
    val antallKilometerEnVei: VerdiFelt<String>,
    val reisemåte: ReisemåteDto? = null,
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
    val hvilkeTransportmidlerBleBenyttet: EnumFlereValgFelt<Transportmiddel>?,
    val unntakFraOffentligTransport: UnntakFraOffentligTransportDto?,
    val unntakFraPrivatBil: EnumFlereValgFelt<ÅrsakKanIkkeBenytteEgenBil>?,
    val offentligTransport: OffentligTransportInfoDto?,
    val privatBil: PrivatBilInfoDto?,
    val drosje: DrosjeInfoDto?,
)

data class OffentligTransportInfoDto(
    val totalUtgifterOffentligTransport: VerdiFelt<String>?,
)

data class PrivatBilInfoDto(
    val benyttetEgenBil: EnumFelt<JaNei>?,
    val betalteForReisen: EnumFelt<JaNei>?,
    val infoBilKunDelerAvStrekning: InfoBilKunDelerAvStrekningDto?,
    val utgifterPrivatBil: UtgifterPrivatBilDto?,
)

data class DrosjeInfoDto(
    val harTTKort: EnumFelt<JaNei>?,
)

data class UnntakFraOffentligTransportDto(
    val årsaker: EnumFlereValgFelt<ÅrsakKanIkkeBenytteOffentligTransport>?,
    val leveringOgHentingIBarnehage: LeveringOgHentingIBarnehageDto?,
)

data class LeveringOgHentingIBarnehageDto(
    val gateadresse: VerdiFelt<String>?,
    val postnummer: VerdiFelt<String>?,
)

data class UtgifterPrivatBilDto(
    val bompenger: VerdiFelt<String>?,
    val ferge: VerdiFelt<String>?,
    val piggdekkavgift: VerdiFelt<String>?,
    val parkering: VerdiFelt<String>?,
    val drivstoffType: EnumFelt<DrivstoffType>?,
)

data class InfoBilKunDelerAvStrekningDto(
    val strekningHvorBilBleBenyttet: VerdiFelt<String>?,
    val antallKilometerKjørt: VerdiFelt<String>?,
)
