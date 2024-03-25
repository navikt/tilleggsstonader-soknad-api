package no.nav.tilleggsstonader.soknad.soknad.barnetilsyn

import no.nav.tilleggsstonader.kontrakter.felles.Hovedytelse
import no.nav.tilleggsstonader.kontrakter.søknad.Dokument
import no.nav.tilleggsstonader.kontrakter.søknad.DokumentasjonFelt
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFelt
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFlereValgFelt
import no.nav.tilleggsstonader.kontrakter.søknad.JaNei
import no.nav.tilleggsstonader.kontrakter.søknad.Vedleggstype
import no.nav.tilleggsstonader.kontrakter.søknad.VerdiFelt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.TypeBarnepass
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.ÅrsakBarnepass
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
            boddSammenhengende = EnumFelt("Bodd sammenhengende?", JaNei.JA, "Ja", emptyList()),
            planleggerBoINorgeNeste12mnd = EnumFelt("Planlegger du å bo i Norge de neste 12 månedene?", JaNei.JA, "Ja", emptyList()),
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
