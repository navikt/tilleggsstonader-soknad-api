package no.nav.tilleggsstonader.soknad.kjøreliste

import java.time.LocalDate

data class RammevedtakDto(
    val reiseId: String,
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
    val innsendtDato: LocalDate?,
    val kanSendeInnKjøreliste: Boolean,
)
