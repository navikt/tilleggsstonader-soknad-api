package no.nav.tilleggsstonader.soknad.soknad.barnetilsyn

import no.nav.tilleggsstonader.kontrakter.felles.Språkkode
import no.nav.tilleggsstonader.kontrakter.søknad.TekstFelt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.AktivitetAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.BarnAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.HovedytelseAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.SøknadsskjemaBarnetilsyn
import no.nav.tilleggsstonader.soknad.person.dto.Barn
import org.springframework.stereotype.Service
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.BarnMedBarnepass as BarnMedBarnepassKontrakt

@Service
class BarnetilsynMapper {

    fun map(dto: SøknadBarnetilsynDto, pdlBarn: Map<String, Barn>, språkkode: Språkkode): SøknadsskjemaBarnetilsyn {
        return SøknadsskjemaBarnetilsyn(
            hovedytelse = mapHovedytelse(dto),
            aktivitet = mapAktivitet(dto),
            barn = BarnAvsnitt(mapBarn(dto, pdlBarn, språkkode)),
            dokumentasjon = dto.dokumentasjon,
        )
    }

    private fun mapHovedytelse(dto: SøknadBarnetilsynDto) =
        HovedytelseAvsnitt(
            hovedytelse = dto.hovedytelse.ytelse,
            boddSammenhengende = dto.hovedytelse.boddSammenhengende,
            planleggerBoINorgeNeste12mnd = dto.hovedytelse.planleggerBoINorgeNeste12mnd,
        )

    private fun mapBarn(
        dto: SøknadBarnetilsynDto,
        pdlBarn: Map<String, Barn>,
        språkkode: Språkkode,
    ) =
        dto.barnMedBarnepass.map {
            val barn = pdlBarn[it.ident] ?: error("Finner ikke barn=${it.ident} i barn fra PDL")
            BarnMedBarnepassKontrakt(
                navn = TekstFelt(labelNavn(språkkode), barn.navn),
                ident = TekstFelt(labelFødselsnummer(språkkode), it.ident),
                type = it.type,
                startetIFemte = it.startetIFemte,
                årsak = it.årsak,
            )
        }

    private fun labelNavn(språkkode: Språkkode) = when (språkkode) {
        Språkkode.NB -> "Navn"
        Språkkode.NN -> "Namn"
    }

    private fun labelFødselsnummer(språkkode: Språkkode) = when (språkkode) {
        Språkkode.NB, Språkkode.NN -> "Fødselsnummer"
    }

    private fun mapAktivitet(dto: SøknadBarnetilsynDto) = AktivitetAvsnitt(
        utdanning = dto.aktivitet.utdanning,
    )
}
