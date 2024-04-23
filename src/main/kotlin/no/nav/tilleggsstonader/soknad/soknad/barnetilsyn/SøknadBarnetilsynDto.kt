package no.nav.tilleggsstonader.soknad.soknad.barnetilsyn

import no.nav.tilleggsstonader.kontrakter.felles.Hovedytelse
import no.nav.tilleggsstonader.kontrakter.søknad.DatoFelt
import no.nav.tilleggsstonader.kontrakter.søknad.DokumentasjonFelt
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFelt
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFlereValgFelt
import no.nav.tilleggsstonader.kontrakter.søknad.JaNei
import no.nav.tilleggsstonader.kontrakter.søknad.SelectFelt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.AnnenAktivitetType
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.TypeBarnepass
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.TypePengestøtte
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.ÅrsakBarnepass
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.ÅrsakOppholdUtenforNorge
import no.nav.tilleggsstonader.soknad.soknad.SøknadValideringException

data class SøknadBarnetilsynDto(
    val hovedytelse: HovedytelseDto,
    val aktivitet: Aktivitet,
    val barnMedBarnepass: List<BarnMedBarnepass>,
    val dokumentasjon: List<DokumentasjonFelt>,
)

data class HovedytelseDto(
    val ytelse: EnumFlereValgFelt<Hovedytelse>,
    val arbeidOgOpphold: ArbeidOgOppholdDto,
)

data class ArbeidOgOppholdDto(
    val jobberIAnnetLand: EnumFelt<JaNei>?,
    val jobbAnnetLand: SelectFelt<String>?,
    val harPengestøtteAnnetLand: EnumFlereValgFelt<TypePengestøtte>?,
    val pengestøtteAnnetLand: SelectFelt<String>?,
    val harOppholdUtenforNorgeSiste12mnd: EnumFelt<JaNei>?,
    val oppholdUtenforNorgeSiste12mnd: List<OppholdUtenforNorgeDto>,
    val harOppholdUtenforNorgeNeste12mnd: EnumFelt<JaNei>?,
    val oppholdUtenforNorgeNeste12mnd: List<OppholdUtenforNorgeDto>,
)

data class OppholdUtenforNorgeDto(
    val land: SelectFelt<String>,
    val årsak: EnumFlereValgFelt<ÅrsakOppholdUtenforNorge>,
    val fom: DatoFelt,
    val tom: DatoFelt,
)

data class Aktivitet(
    val aktiviteter: EnumFlereValgFelt<String>?,
    val annenAktivitet: EnumFelt<AnnenAktivitetType>?,
    val lønnetAktivitet: EnumFelt<JaNei>?,
)

data class BarnMedBarnepass(
    val ident: String,
    val type: EnumFelt<TypeBarnepass>,
    val startetIFemte: EnumFelt<JaNei>?,
    val årsak: EnumFelt<ÅrsakBarnepass>?,
) {
    init {
        if (startetIFemte?.verdi == JaNei.JA && årsak == null) {
            throw SøknadValideringException("Må ha valgt årsak hvis barnet har begynt i 5. klasse")
        }

        if (startetIFemte?.verdi != JaNei.JA && årsak != null) {
            throw SøknadValideringException("Kan ikke sende inn årsak når barnet ikke har begynt i 5. klasse")
        }
    }
}
