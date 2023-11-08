package no.nav.tilleggsstonader.soknad.soknad.barnetilsyn

import no.nav.tilleggsstonader.kontrakter.felles.Hovedytelse
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFelt
import no.nav.tilleggsstonader.kontrakter.søknad.JaNei
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.TypeBarnepass
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.ÅrsakBarnepass

object SøknadBarnetilsynUtil {
    val søknad = SøknadBarnetilsynDto(
        hovedytelse = EnumFelt("Hovedutelse?", Hovedytelse.AAP, "AAP", listOf("Alt1", "Alt2")),
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
            lagBarn("43921075201", EnumFelt("Type barnepass", TypeBarnepass.BARNEHAGE_SFO_AKS, "Svartekst", emptyList())),
        ),
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
