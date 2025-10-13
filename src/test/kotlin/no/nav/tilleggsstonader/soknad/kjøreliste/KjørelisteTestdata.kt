package no.nav.tilleggsstonader.soknad.kjøreliste

import no.nav.tilleggsstonader.kontrakter.søknad.DatoFelt
import no.nav.tilleggsstonader.kontrakter.søknad.Dokument
import no.nav.tilleggsstonader.kontrakter.søknad.DokumentasjonFelt
import no.nav.tilleggsstonader.kontrakter.søknad.NumeriskFelt
import no.nav.tilleggsstonader.kontrakter.søknad.Vedleggstype
import no.nav.tilleggsstonader.libs.utils.dato.juni
import no.nav.tilleggsstonader.soknad.soknad.SøknadMetadataDto
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale
import java.util.UUID
import kotlin.random.Random
import kotlin.random.nextInt

object KjørelisteTestdata {
    private val localeNb = Locale.of("nb")

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

    fun kjørelisteDtoMedReisedagerIPeriode(
        fom: LocalDate,
        tom: LocalDate,
    ): KjørelisteDto =
        KjørelisteDto(
            reisedagerPerUkeAvsnitt = lagUkeliste(fom, tom),
            dokumentasjon = listOf(lagDokumentasjonFelt()),
            søknadMetadata = SøknadMetadataDto(søknadFrontendGitHash = "aabbccd"),
        )

    private data class DagMedUkenummer(
        val dato: LocalDate,
        val ukeNummer: Int,
    )

    fun lagUkeliste(
        fom: LocalDate,
        tom: LocalDate,
    ): List<UkeMedReisedagerDto> {
        val dagerMedUkenummer =
            generateSequence(fom) { it.plusDays(1) }
                .takeWhile { it <= tom }
                .map { DagMedUkenummer(it, it.get(WeekFields.of(DayOfWeek.MONDAY, 1).weekOfWeekBasedYear())) }
                .toList()

        return dagerMedUkenummer
            .groupBy { it.ukeNummer }
            .mapValues { it.value }
            .map { ukeMedDager ->
                UkeMedReisedagerDto(
                    ukeLabel = "Uke ${ukeMedDager.key} (${datoTilTekstUtenÅr(
                        ukeMedDager.value.first().dato,
                    )} - ${datoTilTekstUtenÅr(ukeMedDager.value.last().dato)})",
                    spørsmål = "Hvilke dager kjørte du?",
                    reisedager =
                        ukeMedDager.value.map { dag ->
                            ReisedagDto(
                                dato = datofelt(dag.dato),
                                harKjørt = dag.dato.dayOfWeek in listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                                parkeringsutgift = if (dag.dato.dayOfWeek == DayOfWeek.MONDAY) parkeringsutgift(50) else null,
                            )
                        },
                )
            }
    }

    fun parkeringsutgift(kr: Int = 50) = NumeriskFelt("Parkeringsutgift (kr)", kr)

    fun datofelt(dato: LocalDate) =
        DatoFelt(
            label = "${datoTilTekstUtenÅr(dato)} ${dato.year}",
            verdi = dato,
        )

    fun datoTilTekstUtenÅr(dato: LocalDate) =
        """
            ${dato.dayOfWeek.getDisplayName(
            TextStyle.FULL,
            localeNb,
        ).replaceFirstChar { it.uppercase() }} ${dato.dayOfMonth}. ${dato.month.getDisplayName(
            TextStyle.FULL,
            localeNb,
        )}
        """.trimIndent()
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
