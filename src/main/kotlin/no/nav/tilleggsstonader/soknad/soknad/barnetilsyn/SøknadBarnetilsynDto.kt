package no.nav.tilleggsstonader.soknad.soknad.barnetilsyn

import no.nav.tilleggsstonader.kontrakter.felles.Hovedytelse
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFelt
import no.nav.tilleggsstonader.kontrakter.søknad.JaNei
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.TypeBarnepass
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.ÅrsakBarnepass
import no.nav.tilleggsstonader.soknad.soknad.SøknadValideringException

data class SøknadBarnetilsynDto(
    val hovedytelse: EnumFelt<Hovedytelse>,
    val aktivitet: Aktivitet,
    val barnMedBarnepass: List<BarnMedBarnepass>,
)

data class Aktivitet(
    val utdanning: EnumFelt<JaNei>,
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
