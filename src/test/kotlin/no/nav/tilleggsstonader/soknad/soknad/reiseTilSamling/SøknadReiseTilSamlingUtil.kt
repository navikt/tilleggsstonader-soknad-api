package no.nav.tilleggsstonader.soknad.soknad.reiseTilSamling

import no.nav.tilleggsstonader.kontrakter.søknad.EnumFelt
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFlereValgFelt
import no.nav.tilleggsstonader.kontrakter.søknad.JaNei
import no.nav.tilleggsstonader.kontrakter.søknad.SelectFelt
import no.nav.tilleggsstonader.kontrakter.søknad.VerdiFelt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.AnnenAktivitetType
import no.nav.tilleggsstonader.soknad.soknad.SøknadMetadataDto
import no.nav.tilleggsstonader.soknad.soknad.SøknadTestUtil

object SøknadReiseTilSamlingUtil {
    val søknadReiseTilSamling =
        SøknadReiseTilSamlingDto(
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
            samlinger =
                listOf(
                    SamlingDto(
                        fom = VerdiFelt(label = "Startdato", verdi = "2024-03-01"),
                        tom = VerdiFelt(label = "Sluttdato", verdi = "2024-03-03"),
                        erObligatorisk =
                            EnumFelt(
                                label = "Er samlingen obligatorisk?",
                                verdi = JaNei.JA,
                                svarTekst = "Ja",
                                alternativer = listOf(),
                            ),
                    ),
                    SamlingDto(
                        fom = VerdiFelt(label = "Startdato", verdi = "2024-04-15"),
                        tom = VerdiFelt(label = "Sluttdato", verdi = "2024-04-17"),
                        erObligatorisk =
                            EnumFelt(
                                label = "Er samlingen obligatorisk?",
                                verdi = JaNei.JA,
                                svarTekst = "Ja",
                                alternativer = listOf(),
                            ),
                    ),
                ),
            reiseavstand =
                ReiseavstandDto(
                    antallKilometerEnVei = VerdiFelt(label = "Antall kilometer én vei", verdi = "42"),
                    skalReiseFraFolkeregistrertAdresse =
                        EnumFelt(
                            label = "Reiser du fra din folkeregistrerte adresse?",
                            verdi = JaNei.JA,
                            svarTekst = "Ja",
                            alternativer = emptyList(),
                        ),
                    adresseDetSkalReisesFra =
                        AdresseDto(
                            land = SelectFelt("Land", "NO", "Norge"),
                            gateadresse = VerdiFelt(verdi = "Lurendreiergata 1", label = "Gateadresse"),
                            postnummer = VerdiFelt(verdi = "5132", label = "Postnummer"),
                            poststed = VerdiFelt(verdi = "Pæddekummen", label = "Poststed"),
                        ),
                    aktivitetsadresse =
                        AdresseDto(
                            land = SelectFelt(label = "Land", verdi = "NOR", svarTekst = "Norge"),
                            gateadresse = VerdiFelt(label = "Gateadresse", verdi = "Storgata 1"),
                            postnummer = VerdiFelt(label = "Postnummer", verdi = "0155"),
                            poststed = VerdiFelt(label = "Poststed", verdi = "Oslo"),
                        ),
                ),
            reisemåte =
                ReisemåteDto(
                    kanReiseKollektivt =
                        EnumFelt(
                            label = "Kan du reise kollektivt?",
                            verdi = JaNei.JA,
                            svarTekst = "Ja",
                            alternativer = listOf(),
                        ),
                    totalutgifterKollektivt = VerdiFelt(label = "Totale kollektivutgifter", verdi = "450"),
                    kanBenytteEgenBil = null,
                    kanBenytteDrosje = null,
                ),
            dokumentasjon = listOf(),
            søknadMetadata = SøknadMetadataDto(søknadFrontendGitHash = "aabbccd"),
        )
}
