package no.nav.tilleggsstonader.soknad.soknad.roah

import no.nav.tilleggsstonader.kontrakter.søknad.EnumFelt
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFlereValgFelt
import no.nav.tilleggsstonader.kontrakter.søknad.JaNei
import no.nav.tilleggsstonader.kontrakter.søknad.SelectFelt
import no.nav.tilleggsstonader.kontrakter.søknad.VerdiFelt
import no.nav.tilleggsstonader.kontrakter.søknad.felles.AnnenAktivitetType
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.Transportmiddel
import no.nav.tilleggsstonader.kontrakter.søknad.roah.ROAHTypeReise
import no.nav.tilleggsstonader.soknad.soknad.SøknadMetadataDto
import no.nav.tilleggsstonader.soknad.soknad.SøknadTestUtil
import no.nav.tilleggsstonader.soknad.soknad.reiseTilSamling.AdresseDto
import no.nav.tilleggsstonader.soknad.soknad.reiseTilSamling.OffentligTransportInfoDto
import no.nav.tilleggsstonader.soknad.soknad.reiseTilSamling.ReisemåteDto

object SøknadROAHUtil {
    val søknadROAH =
        SøknadROAHDto(
            hovedytelse = SøknadTestUtil.hovedytelseDto(),
            aktivitet =
                AktivitetDto(
                    aktiviteter =
                        EnumFlereValgFelt(
                            label = "Hvilken aktivitet deltar du i?",
                            verdier = listOf(VerdiFelt("1", "Arbeidsavklaring: 1. jan 2024 - 31. des 2024")),
                            alternativer = listOf("Alt1", "Alt2"),
                        ),
                    annenAktivitet =
                        EnumFelt(
                            label = "Hva slags aktivitet deltar du i?",
                            verdi = AnnenAktivitetType.TILTAK,
                            svarTekst = "Tiltak",
                            alternativer = listOf(),
                        ),
                    lønnetAktivitet =
                        EnumFelt(
                            label = "Er aktiviteten lønnet?",
                            verdi = JaNei.NEI,
                            svarTekst = "Nei",
                            alternativer = listOf(),
                        ),
                ),
            adresser =
                AdresserDto(
                    måBoBorteHjemmefra =
                        EnumFelt(
                            label = "Må du bo borte hjemmefra?",
                            verdi = JaNei.JA,
                            svarTekst = "Ja",
                            alternativer = listOf(),
                        ),
                    periodeMidlertidigBosted =
                        PeriodeMidlertidigBostedDto(
                            fomFlyttedato = VerdiFelt(label = "Flyttet fra", verdi = "2024-08-01"),
                            tomFlyttedato = VerdiFelt(label = "Flytter hjem", verdi = "2025-06-30"),
                        ),
                    adresseOriginaltBosted =
                        AdresseOriginaltBostedDto(
                            skalReiseFraFolkeregistrertAdresse =
                                EnumFelt(
                                    label = "Skal du reise fra folkeregistrert adresse?",
                                    verdi = JaNei.JA,
                                    svarTekst = "Ja",
                                    alternativer = emptyList(),
                                ),
                            adresseOriginaltBosted =
                                AdresseDto(
                                    land = SelectFelt("Land", "NO", "Norge"),
                                    gateadresse = VerdiFelt(verdi = "Lurendreiergata 1", label = "Gateadresse"),
                                    postnummer = VerdiFelt(verdi = "5132", label = "Postnummer"),
                                    poststed = VerdiFelt(verdi = "Pæddekummen", label = "Poststed"),
                                ),
                        ),
                    adresseMidlertidigBosted =
                        AdresseDto(
                            land = SelectFelt("Land", "NO", "Norge"),
                            gateadresse = VerdiFelt(verdi = "Midlertidigveien 2", label = "Gateadresse"),
                            postnummer = VerdiFelt(verdi = "0250", label = "Postnummer"),
                            poststed = VerdiFelt(verdi = "Oslo", label = "Poststed"),
                        ),
                ),
            barnOgSærligeBehov =
                BarnOgSærligeBehovDto(
                    harBarnUnder18SomHarFlyttetMed =
                        EnumFelt(
                            label = "Har du barn under 18 år som har flyttet med?",
                            verdi = JaNei.JA,
                            svarTekst = "Ja",
                            alternativer = listOf(),
                        ),
                    hvilkeBarnFlytterMed =
                        EnumFlereValgFelt(
                            label = "Hvilke barn flytter med?",
                            verdier = listOf(VerdiFelt("01020312345", "Barn 1"), VerdiFelt("01030412345", "Barn 2")),
                            alternativer = listOf("Barn 1", "Barn 2"),
                        ),
                    harBarnHjemmeUnder4Klasse =
                        EnumFelt(
                            label = "Har du barn hjemme under 4. klasse?",
                            verdi = JaNei.NEI,
                            svarTekst = "Nei",
                            alternativer = listOf(),
                        ),
                    harSærligeBehovForFlereHjemreiser =
                        EnumFelt(
                            label = "Har du særlige behov for flere hjemreiser?",
                            verdi = JaNei.NEI,
                            svarTekst = "Nei",
                            alternativer = listOf(),
                        ),
                ),
            reiser =
                listOf(
                    ReiseInfoDto(
                        typeReise =
                            EnumFelt(
                                label = "Type reise",
                                verdi = ROAHTypeReise.OPPSTART,
                                svarTekst = "Oppstart",
                                alternativer = listOf(),
                            ),
                        fom = VerdiFelt(label = "Fra dato", verdi = "2024-08-01"),
                        tom = null,
                        reisemåte =
                            ReisemåteDto(
                                hvilkeTransportmidlerBleBenyttet =
                                    EnumFlereValgFelt(
                                        label = "Hvilke transportmidler ble benyttet?",
                                        verdier = listOf(VerdiFelt(Transportmiddel.OFFENTLIG_TRANSPORT, "Offentlig transport")),
                                        alternativer = listOf(),
                                    ),
                                unntakFraOffentligTransport = null,
                                unntakFraPrivatBil = null,
                                offentligTransport =
                                    OffentligTransportInfoDto(
                                        totalUtgifterOffentligTransport = VerdiFelt(label = "Totale kollektivutgifter", verdi = "450"),
                                    ),
                                privatBil = null,
                                drosje = null,
                            ),
                    ),
                    ReiseInfoDto(
                        typeReise =
                            EnumFelt(
                                label = "Type reise",
                                verdi = ROAHTypeReise.HJEMREISE,
                                svarTekst = "Hjemreise",
                                alternativer = listOf(),
                            ),
                        fom = VerdiFelt(label = "Fra dato", verdi = "2024-10-01"),
                        tom = VerdiFelt(label = "Til dato", verdi = "2024-10-07"),
                        reisemåte = null,
                    ),
                ),
            dokumentasjon = listOf(),
            søknadMetadata = SøknadMetadataDto(søknadFrontendGitHash = "aabbccd"),
        )
}
