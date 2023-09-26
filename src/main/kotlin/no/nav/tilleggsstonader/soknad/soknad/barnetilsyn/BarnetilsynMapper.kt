package no.nav.tilleggsstonader.soknad.soknad.barnetilsyn

import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.Aktivitet
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.BarnMedBarnepass
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.SøknadsskjemaBarnetilsyn
import org.springframework.stereotype.Service

@Service
class BarnetilsynMapper {

    fun map(dto: SøknadBarnetilsynDto): SøknadsskjemaBarnetilsyn {
        return SøknadsskjemaBarnetilsyn(
            hovedytelse = dto.hovedytelse,
            aktivitet = mapAktivitet(dto),
            barn = mapBarn(dto),
        )
    }

    private fun mapBarn(dto: SøknadBarnetilsynDto) =
        dto.barn.map {
            BarnMedBarnepass(
                ident = it.ident,
                type = it.type,
                startetIFemte = it.startetIFemte,
                årsak = it.årsak
        )
    }

    private fun mapAktivitet(dto: SøknadBarnetilsynDto) = Aktivitet(
        utdanning = dto.aktivitet.utdanning
    )
}
