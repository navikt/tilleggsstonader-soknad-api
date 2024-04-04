package no.nav.tilleggsstonader.soknad.soknad.barnetilsyn

import no.nav.tilleggsstonader.kontrakter.felles.Hovedytelse
import no.nav.tilleggsstonader.kontrakter.søknad.DatoFelt
import no.nav.tilleggsstonader.kontrakter.søknad.Dokument
import no.nav.tilleggsstonader.kontrakter.søknad.DokumentasjonFelt
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFelt
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFlereValgFelt
import no.nav.tilleggsstonader.kontrakter.søknad.JaNei
import no.nav.tilleggsstonader.kontrakter.søknad.SelectFelt
import no.nav.tilleggsstonader.kontrakter.søknad.Vedleggstype
import no.nav.tilleggsstonader.kontrakter.søknad.VerdiFelt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.TypePengestøtte
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.TypeBarnepass
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.ÅrsakBarnepass
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.ÅrsakOppholdUtenforNorge
import java.time.LocalDate
import java.util.UUID

object SøknadBarnetilsynUtil {
    val søknad = SøknadBarnetilsynDto(
        hovedytelse = HovedytelseDto(
            ytelse = EnumFlereValgFelt(
                label = "Hovedytelse?",
                verdier = listOf(
                    VerdiFelt(Hovedytelse.AAP, "AAP"),
                    VerdiFelt(Hovedytelse.OVERGANGSSTØNAD, "Overgangsstønad"),
                ),
                alternativer = listOf("Alt1", "Alt2"),
            ),
            arbeidOgOpphold = arbeidOgOppholdDto(),
        ),
        aktivitet = Aktivitet(
            utdanning = EnumFelt(
                "Skal du søke om støtte til pass av barn i forbindelse med denne utdanningen?",
                JaNei.JA,
                "Ja",
                listOf("Alt1", "Alt2"),
            ),
        ),
        barnMedBarnepass = listOf(
            lagBarn("08921997974"),
            lagBarn(
                "43921075201",
                EnumFelt("Type barnepass", TypeBarnepass.BARNEHAGE_SFO_AKS, "Svartekst", emptyList()),
            ),
        ),
        dokumentasjon = listOf(lagDokumentasjonFelt(), lagDokumentasjonFeltBarn()),
    )

    private fun arbeidOgOppholdDto() = ArbeidOgOppholdDto(
        jobberIAnnetLand = EnumFelt("Jobber du i et annet land enn Norge?", JaNei.JA, "Ja", emptyList()),
        jobbAnnetLand = SelectFelt("Hvilket land jobber du i?", "SWE", "Sverige"),
        harPengestøtteAnnetLand = EnumFlereValgFelt(
            "Mottar du pengestøttene fra et annet land enn Norge?",
            listOf(VerdiFelt(TypePengestøtte.SYKEPENGER, "Sykepenger")),
            listOf("Sykepenger", "Annet"),
        ),
        pengestøtteAnnetLand = SelectFelt("Hvilket land mottar du pengestøtte fra?", "SWE", "Sverige"),
        harOppholdUtenforNorgeSiste12mnd = EnumFelt(
            "Jobber du i et annet land enn Norge?",
            JaNei.JA,
            "Ja",
            emptyList(),
        ),
        oppholdUtenforNorgeSiste12mnd = listOf(oppholdUtenforNorgeDto(), oppholdUtenforNorgeDto()),
        harOppholdUtenforNorgeNeste12mnd = EnumFelt(
            "Jobber du i et annet land enn Norge?",
            JaNei.JA,
            "Ja",
            emptyList(),
        ),
        oppholdUtenforNorgeNeste12mnd = listOf(oppholdUtenforNorgeDto()),
    )

    private fun oppholdUtenforNorgeDto() = OppholdUtenforNorgeDto(
        land = SelectFelt("Hvilket land har du oppholdt deg i?", "SWE", "Sverige"),
        årsak = EnumFlereValgFelt(
            "Hva gjorde du i dette landet?",
            listOf(VerdiFelt(ÅrsakOppholdUtenforNorge.JOBB, "Jobb")),
            alternativer = listOf("Jobb", "Studier"),
        ),
        fom = DatoFelt("Fom", LocalDate.of(2024, 1, 1)),
        tom = DatoFelt("Fom", LocalDate.of(2024, 1, 1)),
    )

    private fun lagDokumentasjonFelt() = DokumentasjonFelt(
        type = Vedleggstype.UTGIFTER_PASS_ANNET,
        label = "Vedlegglabel",
        opplastedeVedlegg = listOf(
            Dokument(
                id = UUID.fromString("98fd0f9b-1206-4918-80d9-e76f85ba1b39"),
                "Navn på vedlegg",
            ),
        ),
    )

    private fun lagDokumentasjonFeltBarn() = DokumentasjonFelt(
        type = Vedleggstype.UTGIFTER_PASS_SFO_AKS_BARNEHAGE,
        label = "Vedlegglabel",
        opplastedeVedlegg = listOf(
            Dokument(
                id = UUID.fromString("3e5f0073-036b-4da1-af82-787fecdbb481"),
                "Navn på vedlegg",
            ),
        ),
        barnId = "08921997974",
    )

    fun lagBarn(
        ident: String,
        type: EnumFelt<TypeBarnepass> = defaultTypeBarnepass(),
    ) = BarnMedBarnepass(
        ident = ident,
        type = type,
        startetIFemte = EnumFelt("Har startet i 5. klasse?", JaNei.JA, "Ja", emptyList()),
        årsak = EnumFelt("Årsak?", ÅrsakBarnepass.MYE_BORTE_ELLER_UVANLIG_ARBEIDSTID, "Mye borte", emptyList()),
    )

    private fun defaultTypeBarnepass() =
        EnumFelt("Type barnepass", TypeBarnepass.BARNEHAGE_SFO_AKS, "Svartekst", listOf("Alt1", "Alt2"))
}
