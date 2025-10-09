package no.nav.tilleggsstonader.soknad.kjøreliste

import no.nav.tilleggsstonader.kontrakter.søknad.DatoFelt
import no.nav.tilleggsstonader.kontrakter.søknad.Dokument
import no.nav.tilleggsstonader.kontrakter.søknad.DokumentasjonFelt
import no.nav.tilleggsstonader.kontrakter.søknad.NumeriskFelt
import no.nav.tilleggsstonader.kontrakter.søknad.Vedleggstype
import no.nav.tilleggsstonader.libs.utils.dato.juni
import no.nav.tilleggsstonader.soknad.soknad.SøknadMetadataDto
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID

object KjørelisteTestdata {
    fun kjørelisteDto() =
        KjørelisteDto(
            reisedagerPerUkeAvsnitt =
                listOf(
                    UkeMedReisedagerDto(
                        ukeLabel = "Uke 1 (1. juni - 7. juni)",
                        spørsmål = "Hvilke dager kjørte du?",
                        reisedager =
                            listOf(
                                ReisedagDto(datofelt(1 juni 2025), true, parkeringsutgift = parkeringsutgift(130)),
                                ReisedagDto(datofelt(2 juni 2025), true, parkeringsutgift = parkeringsutgift(40)),
                                ReisedagDto(datofelt(5 juni 2025), true, parkeringsutgift = parkeringsutgift(90)),
                            ),
                    ),
                ),
            dokumentasjon = listOf(lagDokumentasjonFelt()),
            søknadMetadata = SøknadMetadataDto(søknadFrontendGitHash = "aabbccd"),
        )

    fun parkeringsutgift(kr: Int = 50) = NumeriskFelt("Parkeringsutgift (kr)", kr)

    fun datofelt(dato: LocalDate) =
        DatoFelt(
            label =
                """
                ${dato.dayOfWeek.getDisplayName(
                    TextStyle.FULL,
                    Locale.of("nb"),
                ).replaceFirstChar { it.uppercase() }} ${dato.dayOfMonth}. ${dato.month.getDisplayName(
                    TextStyle.FULL,
                    Locale.of("nb"),
                )} ${dato.year}
                """.trimIndent(),
            verdi = dato,
        )
}

private fun lagDokumentasjonFelt() =
    DokumentasjonFelt(
        type = Vedleggstype.PARKERINGSUTGIFT,
        label = "Vedlegglabel",
        opplastedeVedlegg =
            listOf(
                Dokument(
                    id = UUID.fromString("98fd0f9b-1206-4918-80d9-e76f85ba1b39"),
                    "Parkering 1. juni",
                ),
            ),
    )
