package no.nav.tilleggsstonader.soknad.soknad.reiseTilSamling

import no.nav.tilleggsstonader.kontrakter.felles.Språkkode
import no.nav.tilleggsstonader.kontrakter.søknad.DatoFelt
import no.nav.tilleggsstonader.kontrakter.søknad.InnsendtSkjema
import no.nav.tilleggsstonader.kontrakter.søknad.SøknadsskjemaReiseTilSamling
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.Adresse
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.AktivitetAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.AvreiseadresseAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.ReiseMedBilUtgifterAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.ReisemåteAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.Samling
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.TilleggsopplysningerAnnenAktivitetAvsnitt
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
                    avreiseadresse = mapAvreiseadresse(dto.avreiseadresse),
                    reisemåte = mapReisemåte(dto.reisemåte),
                    dokumentasjon = dto.dokumentasjon,
                ),
        )

    private fun mapAktivitet(dto: AktivitetDto) =
        AktivitetAvsnitt(
            aktiviteter = dto.aktiviteter,
            annenAktivitet = dto.annenAktivitet,
            lønnetAktivitet = dto.lønnetAktivitet,
            tilleggsopplysningerAnnenAktivitet =
                dto.tilleggsopplysningerAnnenAktivitet?.let {
                    mapTilleggsopplysningerAnnenAktivitet(
                        it,
                    )
                },
            annenAktivitetTypeUtdanning = dto.annenAktivitetTypeUtdanning,
        )

    private fun mapTilleggsopplysningerAnnenAktivitet(
        dto: TilleggsopplysningerAnnenAktivitetDto,
    ): TilleggsopplysningerAnnenAktivitetAvsnitt =
        TilleggsopplysningerAnnenAktivitetAvsnitt(
            erLærlingEllerLiknende = dto.erLærlingEllerLiknende,
            fårDekketReise = dto.fårDekketReise,
            erUnder25År = dto.erUnder25År,
            måBetaleForReiseTilSkole = dto.måBetaleForReiseTilSkole,
        )

    private fun mapAdresse(dto: AdresseDto) =
        Adresse(
            land = dto.land,
            gateadresse = dto.gateadresse,
            postnummer = dto.postnummer,
            poststed = dto.poststed,
        )

    private fun mapSamling(dto: SamlingDto) =
        Samling(
            fom = dto.fom.let { DatoFelt(label = it.label, verdi = LocalDate.parse(it.verdi)) },
            tom = dto.tom.let { DatoFelt(label = it.label, verdi = LocalDate.parse(it.verdi)) },
            erObligatorisk = dto.erObligatorisk,
            harBruktEkstraReiseDager = dto.harBruktEkstraReiseDager,
            adresse = mapAdresse(dto.adresse),
            antallKilometerEnVei = dto.antallKilometerEnVei,
        )

    private fun mapAvreiseadresse(dto: AvreiseadresseDto) =
        AvreiseadresseAvsnitt(
            skalReiseFraFolkeregistrertAdresse = dto.skalReiseFraFolkeregistrertAdresse,
            adresseDetSkalReisesFra = dto.adresseDetSkalReisesFra?.let { mapAdresse(it) },
        )

    private fun mapReisemåte(dto: ReisemåteDto) =
        ReisemåteAvsnitt(
            kanReiseMedOffentligTransport = dto.kanReiseMedOffentligTransport,
            totalUtgifterOffentligTransport = dto.totalUtgifterOffentligTransport,
            kanIkkeReiseMedOffentligTransportBegrunnelser = dto.kanIkkeReiseMedOffentligTransportBegrunnelser,
            barnehageGateadresse = dto.barnehageGateadresse,
            barnehagePostnummer = dto.barnehagePostnummer,
            kanBenytteEgenBil = dto.kanBenytteEgenBil,
            kanIkkeBenytteEgenBilBegrunnelser = dto.kanIkkeBenytteEgenBilBegrunnelser,
            ønskerDekketUtgifterForDrosje = dto.ønskerDekketUtgifterForDrosje,
            betalerForReiseSelv = dto.betalerForReiseSelv,
            harTTKort = dto.harTTKort,
            reiseMedBilUtgifter = dto.reiseMedBilUtgifter?.let { mapReiseMedBilUtgifter(it) },
        )

    private fun mapReiseMedBilUtgifter(dto: ReiseMedBilUtgifterDto): ReiseMedBilUtgifterAvsnitt =
        ReiseMedBilUtgifterAvsnitt(
            drivstoffType = dto.drivstoffType,
            bompenger = dto.bompenger,
            ferge = dto.ferge,
            piggdekkavgift = dto.piggdekkavgift,
        )
}
