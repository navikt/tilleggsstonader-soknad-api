package no.nav.tilleggsstonader.soknad.soknad

import no.nav.tilleggsstonader.kontrakter.felles.Hovedytelse
import no.nav.tilleggsstonader.kontrakter.søknad.DatoFelt
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFelt
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFlereValgFelt
import no.nav.tilleggsstonader.kontrakter.søknad.JaNei
import no.nav.tilleggsstonader.kontrakter.søknad.SelectFelt
import no.nav.tilleggsstonader.kontrakter.søknad.felles.TypePengestøtte
import no.nav.tilleggsstonader.kontrakter.søknad.felles.ÅrsakOppholdUtenforNorge

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

data class SøknadMetadataDto(
    val søknadFrontendGitHash: String?,
)
