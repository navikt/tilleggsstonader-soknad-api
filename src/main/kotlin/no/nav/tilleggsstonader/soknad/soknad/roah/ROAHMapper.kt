package no.nav.tilleggsstonader.soknad.soknad.roah

import no.nav.tilleggsstonader.kontrakter.felles.Språkkode
import no.nav.tilleggsstonader.kontrakter.søknad.DatoFelt
import no.nav.tilleggsstonader.kontrakter.søknad.InnsendtSkjema
import no.nav.tilleggsstonader.kontrakter.søknad.SøknadsskjemaROAH
import no.nav.tilleggsstonader.kontrakter.søknad.felles.ReisemåteAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.DrosjeInfo
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.InfoBilKunDelerAvStrekning
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.LeveringOgHentingIBarnehage
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.OffentligTransportInfo
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.PrivatBilInfo
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.UnntakFraOffentligTransport
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.UtgifterPrivatBil
import no.nav.tilleggsstonader.kontrakter.søknad.roah.AdresseMidlertidigBostedAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.roah.AdresseOriginaltBostedAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.roah.AdresseOriginaltBostedDetaljerAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.roah.PeriodeMidlertidigBostedAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.roah.ROAHAdresseAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.roah.ROAHAktivitetAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.roah.ROAHBarnOgSærligeBehovAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.roah.ROAHReiseInfo
import no.nav.tilleggsstonader.soknad.soknad.SøknadMapper
import no.nav.tilleggsstonader.soknad.soknad.reiseTilSamling.AdresseDto
import no.nav.tilleggsstonader.soknad.soknad.reiseTilSamling.DrosjeInfoDto
import no.nav.tilleggsstonader.soknad.soknad.reiseTilSamling.InfoBilKunDelerAvStrekningDto
import no.nav.tilleggsstonader.soknad.soknad.reiseTilSamling.OffentligTransportInfoDto
import no.nav.tilleggsstonader.soknad.soknad.reiseTilSamling.PrivatBilInfoDto
import no.nav.tilleggsstonader.soknad.soknad.reiseTilSamling.ReisemåteDto
import no.nav.tilleggsstonader.soknad.soknad.reiseTilSamling.UnntakFraOffentligTransportDto
import no.nav.tilleggsstonader.soknad.soknad.reiseTilSamling.UtgifterPrivatBilDto
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class ROAHMapper {
    fun map(
        ident: String,
        mottattTidspunkt: LocalDateTime,
        dto: SøknadROAHDto,
    ): InnsendtSkjema<SøknadsskjemaROAH> =
        InnsendtSkjema(
            ident = ident,
            mottattTidspunkt = mottattTidspunkt,
            språk = Språkkode.NB,
            skjema =
                SøknadsskjemaROAH(
                    hovedytelse = SøknadMapper.mapHovedytelse(dto.hovedytelse),
                    aktivitet = mapAktivitet(dto.aktivitet),
                    adresser = mapAdresser(dto.adresser),
                    barnOgSærligeBehov = mapBarnOgSærligeBehov(dto.barnOgSærligeBehov),
                    reiser = dto.reiser.map { mapReise(it) },
                    dokumentasjon = dto.dokumentasjon,
                ),
        )

    private fun mapAktivitet(dto: AktivitetDto) =
        ROAHAktivitetAvsnitt(
            aktiviteter = dto.aktiviteter,
            annenAktivitet = dto.annenAktivitet,
            lønnetAktivitet = dto.lønnetAktivitet,
        )

    private fun mapAdresser(dto: AdresserDto) =
        ROAHAdresseAvsnitt(
            måBoBorteHjemmefra = dto.måBoBorteHjemmefra,
            periodeMidlertidigBosted =
                dto.periodeMidlertidigBosted?.let {
                    PeriodeMidlertidigBostedAvsnitt(
                        fomFlyttedato = it.fomFlyttedato?.let { dato -> DatoFelt(dato.label, LocalDate.parse(dato.verdi)) },
                        tomFlyttedato = it.tomFlyttedato?.let { dato -> DatoFelt(dato.label, LocalDate.parse(dato.verdi)) },
                    )
                },
            adresseOriginaltBosted =
                dto.adresseOriginaltBosted?.let {
                    AdresseOriginaltBostedAvsnitt(
                        skalReiseFraFolkeregistrertAdresse = it.skalReiseFraFolkeregistrertAdresse,
                        adresseOriginaltBosted = it.adresseOriginaltBosted?.let { adresse -> mapAdresseOriginaltBosted(adresse) },
                    )
                },
            adresseMidlertidigBosted = dto.adresseMidlertidigBosted?.let { mapAdresseMidlertidigBosted(it) },
        )

    private fun mapBarnOgSærligeBehov(dto: BarnOgSærligeBehovDto) =
        ROAHBarnOgSærligeBehovAvsnitt(
            harBarnUnder18SomHarFlyttetMed = dto.harBarnUnder18SomHarFlyttetMed,
            hvilkeBarnFlytterMed = dto.hvilkeBarnFlytterMed,
            harBarnHjemmeUnder4Klasse = dto.harBarnHjemmeUnder4Klasse,
            harSærligeBehovForFlereHjemreiser = dto.harSærligeBehovForFlereHjemreiser,
        )

    private fun mapReise(dto: ReiseInfoDto) =
        ROAHReiseInfo(
            typeReise = dto.typeReise,
            fom = dto.fom?.let { DatoFelt(it.label, LocalDate.parse(it.verdi)) },
            tom = dto.tom?.let { DatoFelt(it.label, LocalDate.parse(it.verdi)) },
            reisemåte = dto.reisemåte?.let { mapReisemåte(it) },
        )

    private fun mapAdresseOriginaltBosted(dto: AdresseDto) =
        AdresseOriginaltBostedDetaljerAvsnitt(
            land = dto.land,
            gateadresse = dto.gateadresse,
            postnummer = dto.postnummer,
            poststed = dto.poststed,
        )

    private fun mapAdresseMidlertidigBosted(dto: AdresseDto) =
        AdresseMidlertidigBostedAvsnitt(
            land = dto.land,
            gateadresse = dto.gateadresse,
            postnummer = dto.postnummer,
            poststed = dto.poststed,
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
