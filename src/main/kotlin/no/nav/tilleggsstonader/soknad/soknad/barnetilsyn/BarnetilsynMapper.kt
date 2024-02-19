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
            hovedytelse = mapHovedytelse(dto),
            aktivitet = mapAktivitet(dto),
            barn = BarnAvsnitt(mapBarn(dto)),
            dokumentasjon = dto.dokumentasjon,
        )
    }

    private fun mapHovedytelse(dto: SøknadBarnetilsynDto) =
        HovedytelseAvsnitt(
            hovedytelse = dto.hovedytelse.ytelse,
            boddSammenhengende = dto.hovedytelse.boddSammenhengende,
            planleggerBoINorgeNeste12mnd = dto.hovedytelse.planleggerBoINorgeNeste12mnd,
        )

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
