package no.nav.tilleggsstonader.soknad.kjøreliste

import java.time.LocalDate

data class RammevedtakDto(
    val id: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val reisedagerPerUke: Int,
    val aktivitetsadresse: String,
    val aktivitetsnavn: String,
    val uker: List<RammevedtakUkeDto>,
)

data class RammevedtakUkeDto(
    val fom: LocalDate,
    val tom: LocalDate,
    val ukeNummer: Int,
)

val rammevedtakDtoMock =
    listOf(
        RammevedtakDto(
            id = "1",
            fom = LocalDate.of(2025, 1, 1),
            tom = LocalDate.of(2025, 2, 6),
            reisedagerPerUke = 3,
            aktivitetsadresse = "Tiurveien 34, 0356 Oslo",
            aktivitetsnavn = "Arbeidstrening",
            uker =
                listOf(
                    RammevedtakUkeDto(
                        fom = LocalDate.of(2025, 1, 1),
                        tom = LocalDate.of(2025, 1, 5),
                        ukeNummer = 1,
                    ),
                    RammevedtakUkeDto(
                        fom = LocalDate.of(2025, 1, 6),
                        tom = LocalDate.of(2025, 1, 12),
                        ukeNummer = 2,
                    ),
                    RammevedtakUkeDto(
                        fom = LocalDate.of(2025, 1, 13),
                        tom = LocalDate.of(2025, 1, 19),
                        ukeNummer = 3,
                    ),
                    RammevedtakUkeDto(
                        fom = LocalDate.of(2025, 1, 20),
                        tom = LocalDate.of(2025, 1, 26),
                        ukeNummer = 4,
                    ),
                    RammevedtakUkeDto(
                        fom = LocalDate.of(2025, 1, 27),
                        tom = LocalDate.of(2025, 2, 2),
                        ukeNummer = 5,
                    ),
                    RammevedtakUkeDto(
                        fom = LocalDate.of(2025, 2, 3),
                        tom = LocalDate.of(2025, 2, 6),
                        ukeNummer = 6,
                    ),
                ),
        ),
        RammevedtakDto(
            id = "2",
            fom = LocalDate.of(2025, 2, 10),
            tom = LocalDate.of(2025, 2, 16),
            reisedagerPerUke = 3,
            aktivitetsadresse = "Drammensveien 1, 0356 Oslo",
            aktivitetsnavn = "Tiltak",
            uker =
                listOf(
                    RammevedtakUkeDto(
                        fom = LocalDate.of(2025, 2, 10),
                        tom = LocalDate.of(2025, 2, 16),
                        ukeNummer = 7,
                    ),
                ),
        ),
    )
