package no.nav.tilleggsstonader.soknad.soknad.barnetilsyn

import no.nav.tilleggsstonader.kontrakter.søknad.TekstFelt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.Aktivitet
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.SøknadsskjemaBarnetilsyn
import org.springframework.stereotype.Service
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.BarnMedBarnepass as BarnMedBarnepassKontrakt

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
            BarnMedBarnepassKontrakt(
                navn = TekstFelt("Navn", "Navn"), // TODO navn
                ident = TekstFelt("Fødselsnummer", it.ident), // TODO må kanskje inn med språk-riktig-label her?
                type = it.type,
                startetIFemte = it.startetIFemte,
                årsak = it.årsak,
            )
        }

    private fun mapAktivitet(dto: SøknadBarnetilsynDto) = Aktivitet(
        utdanning = dto.aktivitet.utdanning,
    )
}
