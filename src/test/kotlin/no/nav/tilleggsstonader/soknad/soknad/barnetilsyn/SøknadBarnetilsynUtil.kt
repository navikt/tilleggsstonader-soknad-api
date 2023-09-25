package no.nav.tilleggsstonader.soknad.soknad.barnetilsyn

import no.nav.tilleggsstonader.kontrakter.felles.Hovedytelse
import no.nav.tilleggsstonader.kontrakter.søknad.BooleanFelt
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFelt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.BarnMedBarnepass
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.TypeBarnepass
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.ÅrsakBarnepass

object SøknadBarnetilsynUtil {
    val søknad = SøknadBarnetilsynDto(
        hovedytelse = EnumFelt("Hovedutelse?", Hovedytelse.AAP, "AAP"),
        utdanning = BooleanFelt(
            "Skal du søke om støtte til pass av barn i forbindelse med denne utdanningen?",
            true,
        ),
        barn = listOf(
            BarnMedBarnepass(
                personIdent = "barn1",
                type = EnumFelt("", TypeBarnepass.BARNEHAGE_SFO_AKS, "Svartekst"),
                startetIFemte = BooleanFelt("Har startet i 5. klasse?", true),
                årsak = EnumFelt("Årsak?", ÅrsakBarnepass.MYE_BORTE_ELLER_UVANLIG_ARBEIDSTID, "Mye borte"),
            ),
        ),
    )
}
