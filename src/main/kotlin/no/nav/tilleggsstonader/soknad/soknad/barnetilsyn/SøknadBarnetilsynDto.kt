package no.nav.tilleggsstonader.soknad.soknad.barnetilsyn

import no.nav.tilleggsstonader.kontrakter.felles.Hovedytelse
import no.nav.tilleggsstonader.kontrakter.søknad.BooleanFelt
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFelt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.BarnMedBarnepass
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.TypeBarnepass
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.ÅrsakBarnepass
import no.nav.tilleggsstonader.soknad.soknad.SøknadValideringException

data class SøknadBarnetilsynDto(
    val hovedytelse: EnumFelt<Hovedytelse>,
    val utdanning: BooleanFelt,
    val barn: List<BarnMedBarnepass>,
)

data class BarnMedBarnepass(
    val personIdent: String,
    val type: EnumFelt<TypeBarnepass>,
    val startetIFemte: BooleanFelt?,
    val årsak: EnumFelt<ÅrsakBarnepass>?,
) {
    init {
        if (startetIFemte?.verdi == true && årsak == null) {
            throw SøknadValideringException("Må ha valgt årsak hvis barnet har begynt i 5. klasse")
        }

        if (startetIFemte?.verdi != true && årsak != null) {
            throw SøknadValideringException("Kan ikke sende inn årsak når barnet har begynt i 5. klasse")
        }
    }
}
