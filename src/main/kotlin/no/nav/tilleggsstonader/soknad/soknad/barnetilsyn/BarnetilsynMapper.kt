package no.nav.tilleggsstonader.soknad.soknad.barnetilsyn

import no.nav.tilleggsstonader.kontrakter.felles.Språkkode
import no.nav.tilleggsstonader.kontrakter.søknad.Søknadsskjema
import no.nav.tilleggsstonader.kontrakter.søknad.SøknadsskjemaBarnetilsyn
import no.nav.tilleggsstonader.kontrakter.søknad.TekstFelt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.AktivitetAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.BarnAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.Utgifter
import no.nav.tilleggsstonader.soknad.person.dto.Barn
import no.nav.tilleggsstonader.soknad.soknad.SøknadMapper
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.BarnMedBarnepass as BarnMedBarnepassKontrakt

@Service
class BarnetilsynMapper {
    fun map(
        ident: String,
        mottattTidspunkt: LocalDateTime,
        pdlBarn: Map<String, Barn>,
        dto: SøknadBarnetilsynDto,
    ): Søknadsskjema<SøknadsskjemaBarnetilsyn> {
        val språkkode = Språkkode.NB
        return Søknadsskjema(
            ident = ident,
            mottattTidspunkt = mottattTidspunkt,
            språk = språkkode,
            skjema =
                SøknadsskjemaBarnetilsyn(
                    hovedytelse = SøknadMapper.mapHovedytelse(dto.hovedytelse),
                    aktivitet = mapAktivitet(dto),
                    barn = BarnAvsnitt(mapBarn(dto, pdlBarn, språkkode)),
                    dokumentasjon = dto.dokumentasjon,
                ),
        )
    }

    private fun mapBarn(
        dto: SøknadBarnetilsynDto,
        pdlBarn: Map<String, Barn>,
        språkkode: Språkkode,
    ) = dto.barnMedBarnepass.map {
        val barn = pdlBarn[it.ident] ?: error("Finner ikke barn=${it.ident} i barn fra PDL")
        BarnMedBarnepassKontrakt(
            navn = TekstFelt(labelNavn(språkkode), barn.visningsnavn),
            ident = TekstFelt(labelFødselsnummer(språkkode), it.ident),
            type = it.type,
            utgifter =
                it.utgifter?.let { utgifter ->
                    Utgifter(
                        harUtgifterTilPassHelePerioden = utgifter.harUtgifterTilPassHelePerioden,
                        fom = utgifter.fom,
                        tom = utgifter.tom,
                    )
                },
            startetIFemte = it.startetIFemte,
            årsak = it.årsak,
        )
    }

    private fun labelNavn(språkkode: Språkkode) =
        when (språkkode) {
            Språkkode.NB -> "Navn"
            Språkkode.NN -> "Namn"
        }

    private fun labelFødselsnummer(språkkode: Språkkode) =
        when (språkkode) {
            Språkkode.NB, Språkkode.NN -> "Fødselsnummer"
        }

    private fun mapAktivitet(dto: SøknadBarnetilsynDto) =
        AktivitetAvsnitt(
            aktiviteter = dto.aktivitet.aktiviteter,
            annenAktivitet = dto.aktivitet.annenAktivitet,
            lønnetAktivitet = dto.aktivitet.lønnetAktivitet,
        )
}
