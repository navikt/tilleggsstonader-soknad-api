package no.nav.tilleggsstonader.soknad.soknad.reiseTilSamling

import no.nav.tilleggsstonader.kontrakter.felles.Språkkode
import no.nav.tilleggsstonader.kontrakter.søknad.DatoFelt
import no.nav.tilleggsstonader.kontrakter.søknad.InnsendtSkjema
import no.nav.tilleggsstonader.kontrakter.søknad.SøknadsskjemaReiseTilSamling
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.AdresseAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.AktivitetAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.ReiseavstandAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.ReisemåteAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.Samling
import no.nav.tilleggsstonader.soknad.soknad.SøknadMapper
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class ReiseTilSamlingMapper {
    fun map(
        ident: String,
        mottattTidspunkt: LocalDateTime,
        dto: SøknadReiseTilSamlingDto,
    ): InnsendtSkjema<SøknadsskjemaReiseTilSamling> =
        InnsendtSkjema(
            ident = ident,
            mottattTidspunkt = mottattTidspunkt,
            språk = Språkkode.NB,
            skjema =
                SøknadsskjemaReiseTilSamling(
                    hovedytelse = SøknadMapper.mapHovedytelse(dto.hovedytelse),
                    aktivitet = mapAktivitet(dto.aktivitet),
                    samlinger = dto.samlinger.map { mapSamling(it) },
                    reiseavstand = mapReiseavstand(dto.reiseavstand),
                    reisemåte = mapReisemåte(dto.reisemåte),
                    dokumentasjon = dto.dokumentasjon,
                ),
        )

    private fun mapAktivitet(dto: AktivitetDto) =
        AktivitetAvsnitt(
            aktiviteter = dto.aktiviteter,
            annenAktivitet = dto.annenAktivitet,
            lønnetAktivitet = dto.lønnetAktivitet,
        )

    private fun mapAdresse(dto: AdresseDto) =
        AdresseAvsnitt(
            land = dto.land,
            gateadresse = dto.gateadresse,
            postnummer = dto.postnummer,
            poststed = dto.poststed,
        )

    private fun mapSamling(dto: SamlingDto) =
        Samling(
            fom = dto.fom?.let { DatoFelt(label = it.label, verdi = LocalDate.parse(it.verdi)) },
            tom = dto.tom?.let { DatoFelt(label = it.label, verdi = LocalDate.parse(it.verdi)) },
        )

    private fun mapReiseavstand(dto: ReiseavstandDto) =
        ReiseavstandAvsnitt(
            antallKilometerEnVei = dto.antallKilometerEnVei,
            reiseFraFolkeregistrertAdr = dto.reiseFraFolkeregistrertAdr,
            adresseDetSkalReisesFra = mapAdresse(dto.adresseDetSkalReisesFra),
            aktivitetsadresse = mapAdresse(dto.aktivitetsadresse),
        )

    private fun mapReisemåte(dto: ReisemåteDto) =
        ReisemåteAvsnitt(
            kanReiseKollektivt = dto.kanReiseKollektivt,
            totalutgifterKollektivt = dto.totalutgifterKollektivt,
            kanBenytteEgenBil = dto.kanBenytteEgenBil,
            kanBenytteDrosje = dto.kanBenytteDrosje,
        )
}
