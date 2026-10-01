package no.nav.tilleggsstonader.soknad.soknad.reiseTilSamling

import no.nav.tilleggsstonader.kontrakter.felles.Språkkode
import no.nav.tilleggsstonader.kontrakter.søknad.DatoFelt
import no.nav.tilleggsstonader.kontrakter.søknad.InnsendtSkjema
import no.nav.tilleggsstonader.kontrakter.søknad.SøknadsskjemaReiseTilSamling
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.Adresse
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.AvreiseadresseAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.DrosjeInfo
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.InfoBilKunDelerAvStrekning
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.LeveringOgHentingIBarnehage
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.OffentligTransportInfo
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.PrivatBilInfo
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.ReiseTilSamlingAktivitetAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.ReisemåteAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.Samling
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.TilleggsopplysningerAnnenAktivitetAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.UnntakFraOffentligTransport
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.UtgifterPrivatBil
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
                    dokumentasjon = dto.dokumentasjon,
                ),
        )

    private fun mapAktivitet(dto: AktivitetDto) =
        ReiseTilSamlingAktivitetAvsnitt(
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
            adresse = mapAdresse(dto.adresse),
            antallKilometerEnVei = dto.antallKilometerEnVei,
            reisemåte = dto.reisemåte?.let { mapReisemåte(it) },
        )

    private fun mapAvreiseadresse(dto: AvreiseadresseDto) =
        AvreiseadresseAvsnitt(
            skalReiseFraFolkeregistrertAdresse = dto.skalReiseFraFolkeregistrertAdresse,
            adresseDetSkalReisesFra = dto.adresseDetSkalReisesFra?.let { mapAdresse(it) },
        )

    private fun mapReisemåte(dto: ReisemåteDto) =
        ReisemåteAvsnitt(
            hvilkeTransportmidlerBleBenyttet = dto.hvilkeTransportmidlerBleBenyttet,
            unntakFraOffentligTransport = dto.unntakFraOffentligTransport?.let { mapUnntakFraOffentligTransport(it) },
            unntakFraPrivatBil = dto.unntakFraPrivatBil,
            offentligTransport = dto.offentligTransport?.let { mapOffentligTransport(it) },
            privatBil = dto.privatBil?.let { mapPrivatBil(it) },
            drosje = dto.drosje?.let { mapDrosje(it) },
        )

    private fun mapUnntakFraOffentligTransport(dto: UnntakFraOffentligTransportDto): UnntakFraOffentligTransport =
        UnntakFraOffentligTransport(
            årsaker = dto.årsaker,
            leveringOgHentingIBarnehage =
                dto.leveringOgHentingIBarnehage?.let {
                    LeveringOgHentingIBarnehage(
                        gateadresse = it.gateadresse,
                        postnummer = it.postnummer,
                    )
                },
        )

    private fun mapOffentligTransport(dto: OffentligTransportInfoDto): OffentligTransportInfo =
        OffentligTransportInfo(
            totalUtgifterOffentligTransport = dto.totalUtgifterOffentligTransport,
        )

    private fun mapPrivatBil(dto: PrivatBilInfoDto): PrivatBilInfo =
        PrivatBilInfo(
            benyttetEgenBil = dto.benyttetEgenBil,
            betalteForReisen = dto.betalteForReisen,
            infoBilKunDelerAvStrekning = dto.infoBilKunDelerAvStrekning?.let { mapInfoBilKunDelerAvStrekning(it) },
            utgifterPrivatBil = dto.utgifterPrivatBil?.let { mapUtgifterPrivatBil(it) },
        )

    private fun mapDrosje(dto: DrosjeInfoDto): DrosjeInfo =
        DrosjeInfo(
            harTTKort = dto.harTTKort,
        )

    private fun mapUtgifterPrivatBil(dto: UtgifterPrivatBilDto): UtgifterPrivatBil =
        UtgifterPrivatBil(
            drivstoffType = dto.drivstoffType,
            bompenger = dto.bompenger,
            ferge = dto.ferge,
            piggdekkavgift = dto.piggdekkavgift,
            parkering = dto.parkering,
        )

    private fun mapInfoBilKunDelerAvStrekning(dto: InfoBilKunDelerAvStrekningDto): InfoBilKunDelerAvStrekning =
        InfoBilKunDelerAvStrekning(
            strekningHvorBilBleBenyttet = dto.strekningHvorBilBleBenyttet,
            antallKilometerKjørt = dto.antallKilometerKjørt,
        )
}
