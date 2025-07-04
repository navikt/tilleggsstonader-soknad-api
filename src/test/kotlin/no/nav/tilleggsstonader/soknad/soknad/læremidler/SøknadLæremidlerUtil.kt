package no.nav.tilleggsstonader.soknad.soknad.læremidler

import no.nav.tilleggsstonader.kontrakter.søknad.EnumFelt
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFlereValgFelt
import no.nav.tilleggsstonader.kontrakter.søknad.JaNei
import no.nav.tilleggsstonader.kontrakter.søknad.VerdiFelt
import no.nav.tilleggsstonader.kontrakter.søknad.læremidler.AnnenUtdanningType
import no.nav.tilleggsstonader.kontrakter.søknad.læremidler.HarRettTilUtstyrsstipend
import no.nav.tilleggsstonader.soknad.soknad.SøknadMetadataDto
import no.nav.tilleggsstonader.soknad.soknad.SøknadTestUtil

object SøknadLæremidlerUtil {
    val søknad =
        SøknadLæremidlerDto(
            hovedytelse = SøknadTestUtil.hovedytelseDto(),
            utdanning =
                Utdanning(
                    aktiviteter =
                        EnumFlereValgFelt(
                            "Hvilken utdanning eller opplæring søker du om støtte til læremidler for",
                            listOf(
                                VerdiFelt("1", "Høyere utdanning: 22. april 2024 - 22. april 2024"),
                                VerdiFelt("ANNET", "Annet"),
                            ),
                            listOf("Alt1: 22. april 2024 - 22. april 2024", "Alt2: 22. april 2024 - 22. april 2024"),
                        ),
                    annenUtdanning =
                        EnumFelt(
                            label = "Hva slags utdanning eller opplæring skal du ta?",
                            verdi = AnnenUtdanningType.FAGSKOLE_HØGSKOLE_UNIVERSITET,
                            svarTekst = "Utdanning på fagskole, høgskole eller universitet",
                            alternativer = listOf(),
                        ),
                    harRettTilUtstyrsstipend =
                        HarRettTilUtstyrsstipend(
                            erLærlingEllerLiknende =
                                EnumFelt(
                                    label = "Er du lærling, lærekandidat, praksisbrevkandidat eller kandidat for fagbrev på jobb?",
                                    verdi = JaNei.JA,
                                    svarTekst = "Ja",
                                    alternativer = listOf(),
                                ),
                            harTidligereFullførtVgs =
                                EnumFelt(
                                    label = "Har du tidligere fullført videregående skole?",
                                    verdi = JaNei.JA,
                                    svarTekst = "Ja",
                                    alternativer = listOf(),
                                ),
                        ),
                    harFunksjonsnedsettelse =
                        EnumFelt(
                            label = "Har du en funksjonsnedsettelse som gir deg særlig store utgifter til læremidler?",
                            verdi = JaNei.JA,
                            svarTekst = "Ja",
                            alternativer = listOf(),
                        ),
                ),
            dokumentasjon = listOf(),
            søknadMetadata =
                SøknadMetadataDto(
                    søknadFrontendGitHash = "aabbccd",
                ),
        )
}
