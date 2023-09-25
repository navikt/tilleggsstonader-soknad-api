package no.nav.tilleggsstonader.soknad.soknad.barnetilsyn

import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.SøknadsskjemaBarnetilsyn
import org.springframework.stereotype.Service

@Service
class BarnetilsynMapper {

    fun map(dto: SøknadBarnetilsynDto): SøknadsskjemaBarnetilsyn {
        return SøknadsskjemaBarnetilsyn(
            hovedytelse = dto.hovedytelse,
            utdanning = dto.utdanning,
            barn = dto.barn,
        )
    }
}
