package no.nav.tilleggsstonader.soknad.soknad

import no.nav.tilleggsstonader.kontrakter.søknad.felles.HovedytelseAvsnitt
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.ArbeidOgOppholdMapper

object SøknadMapper {
    fun mapHovedytelse(hovedytelse: HovedytelseDto) =
        HovedytelseAvsnitt(
            hovedytelse = hovedytelse.ytelse,
            arbeidOgOpphold = ArbeidOgOppholdMapper.mapArbeidOgOpphold(hovedytelse.arbeidOgOpphold),
        )
}
