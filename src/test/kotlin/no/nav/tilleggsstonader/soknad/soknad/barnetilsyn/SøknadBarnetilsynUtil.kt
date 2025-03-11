package no.nav.tilleggsstonader.soknad.soknad.barnetilsyn

import no.nav.tilleggsstonader.kontrakter.søknad.DatoFelt
import no.nav.tilleggsstonader.kontrakter.søknad.Dokument
import no.nav.tilleggsstonader.kontrakter.søknad.DokumentasjonFelt
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFelt
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFlereValgFelt
import no.nav.tilleggsstonader.kontrakter.søknad.JaNei
import no.nav.tilleggsstonader.kontrakter.søknad.Vedleggstype
import no.nav.tilleggsstonader.kontrakter.søknad.VerdiFelt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.AnnenAktivitetType
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.TypeBarnepass
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.ÅrsakBarnepass
import no.nav.tilleggsstonader.soknad.soknad.SøknadTestUtil
import java.time.LocalDate
import java.util.UUID

object SøknadBarnetilsynUtil {
    val søknad =
        SøknadBarnetilsynDto(
            hovedytelse = SøknadTestUtil.hovedytelseDto(),
            aktivitet =
                Aktivitet(
                    aktiviteter =
                        EnumFlereValgFelt(
                            "Hvilken aktivitet søker du om støtte i forbindelse med?",
                            listOf(
                                VerdiFelt("1", "Aktivitet: 22. april 2024 - 22. april 2024"),
                                VerdiFelt("ANNET", "Annet"),
                            ),
                            listOf("Alt1: 22. april 2024 - 22. april 2024", "Alt2: 22. april 2024 - 22. april 2024"),
                        ),
                    annenAktivitet =
                        EnumFelt(
                            "Hvilken arbeidsrettet aktivitet har du? ",
                            AnnenAktivitetType.TILTAK,
                            "Tiltak / arbeidsrettet aktivitet",
                            listOf(),
                        ),
                    lønnetAktivitet = EnumFelt("Mottar du lønn gjennom ett tiltak?", JaNei.NEI, "Nei", listOf()),
                ),
            barnMedBarnepass =
                listOf(
                    lagBarn("08921997974"),
                    lagBarn(
                        "43921075201",
                        EnumFelt("Type barnepass", TypeBarnepass.BARNEHAGE_SFO_AKS, "Svartekst", emptyList()),
                    ),
                ),
            dokumentasjon = listOf(lagDokumentasjonFelt(), lagDokumentasjonFeltBarn()),
        )

    private fun lagDokumentasjonFelt() =
        DokumentasjonFelt(
            type = Vedleggstype.UTGIFTER_PASS_PRIVAT,
            label = "Vedlegglabel",
            opplastedeVedlegg =
                listOf(
                    Dokument(
                        id = UUID.fromString("98fd0f9b-1206-4918-80d9-e76f85ba1b39"),
                        "Navn på vedlegg",
                    ),
                ),
        )

    private fun lagDokumentasjonFeltBarn() =
        DokumentasjonFelt(
            type = Vedleggstype.UTGIFTER_PASS_SFO_AKS_BARNEHAGE,
            label = "Vedlegglabel",
            opplastedeVedlegg =
                listOf(
                    Dokument(
                        id = UUID.fromString("3e5f0073-036b-4da1-af82-787fecdbb481"),
                        "Navn på vedlegg",
                    ),
                ),
            barnId = "08921997974",
        )

    fun lagBarn(
        ident: String,
        type: EnumFelt<TypeBarnepass> = defaultTypeBarnepass(),
    ) = BarnMedBarnepass(
        ident = ident,
        type = type,
        utgifter =
            Utgifter(
                harUtgifterTilPass = EnumFelt("Har utgifter?", JaNei.NEI, "Nei", emptyList()),
                fom = DatoFelt(label = "Fra", LocalDate.of(2025, 9, 9)),
                tom = DatoFelt(label = "Til", LocalDate.of(2025, 10, 9)),
            ),
        startetIFemte = EnumFelt("Har startet i 5. klasse?", JaNei.JA, "Ja", emptyList()),
        årsak = EnumFelt("Årsak?", ÅrsakBarnepass.MYE_BORTE_ELLER_UVANLIG_ARBEIDSTID, "Mye borte", emptyList()),
    )

    private fun defaultTypeBarnepass() = EnumFelt("Type barnepass", TypeBarnepass.BARNEHAGE_SFO_AKS, "Svartekst", listOf("Alt1", "Alt2"))
}
