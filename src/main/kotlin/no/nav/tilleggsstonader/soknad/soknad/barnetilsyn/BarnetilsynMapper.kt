package no.nav.tilleggsstonader.soknad.soknad.barnetilsyn

import no.nav.tilleggsstonader.kontrakter.søknad.TekstFelt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.AktivitetAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.BarnAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.HovedytelseAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.SøknadsskjemaBarnetilsyn
import org.springframework.stereotype.Service
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.BarnMedBarnepass as BarnMedBarnepassKontrakt

@Service
class BarnetilsynMapper {

    fun map(dto: SøknadBarnetilsynDto): SøknadsskjemaBarnetilsyn {
        return SøknadsskjemaBarnetilsyn(
            hovedytelse = HovedytelseAvsnitt(dto.hovedytelse),
            aktivitet = mapAktivitet(dto),
            barn = BarnAvsnitt(mapBarn(dto)),
            dokumentasjon = emptyList() //todo hack for build
        )
    }

    private fun mapBarn(dto: SøknadBarnetilsynDto) =
        dto.barnMedBarnepass.map {
            BarnMedBarnepassKontrakt(
                navn = TekstFelt("Navn", "Navn"), // TODO navn
                ident = TekstFelt("Fødselsnummer", it.ident), // TODO må kanskje inn med språk-riktig-label her?
                type = it.type,
                startetIFemte = it.startetIFemte,
                årsak = it.årsak,
            )
        }

    private fun mapAktivitet(dto: SøknadBarnetilsynDto) = AktivitetAvsnitt(
        utdanning = dto.aktivitet.utdanning,
    )
}
