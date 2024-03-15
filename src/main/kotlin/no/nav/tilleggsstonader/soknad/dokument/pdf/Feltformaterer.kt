package no.nav.tilleggsstonader.soknad.dokument.pdf

import no.nav.tilleggsstonader.soknad.util.DatoFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

object Feltformaterer {

    fun mapVerdi(verdi: Any?): String {
        if (verdi == null) {
            return ""
        }
        return when (verdi) {
            is Int,
            is String,
            -> verdi.toString()

            is Month -> tilUtskriftsformat(verdi)
            is Boolean -> tilUtskriftsformat(verdi)
            is Double -> tilUtskriftsformat(verdi)
            is Collection<*> -> verdi.joinToString(", ") { mapVerdi(it!!) }
            is LocalDate -> tilUtskriftsformat(verdi)
            is LocalDateTime -> tilUtskriftsformat(verdi)
            else -> error("Kan ikke mappe $verdi")
        }
    }

    private fun tilUtskriftsformat(verdi: Boolean) = if (verdi) "Ja" else "Nei"
    private fun tilUtskriftsformat(verdi: Double) = String.format("%.2f", verdi).replace(".", ",")
    private fun tilUtskriftsformat(verdi: Month) = verdi.getDisplayName(TextStyle.FULL, Locale("no"))
    private fun tilUtskriftsformat(verdi: LocalDateTime) =
        verdi.format(DatoFormat.DATE_TIME_FORMAT_NORSK)

    private fun tilUtskriftsformat(verdi: LocalDate): String =
        verdi.format(DatoFormat.DATE_FORMAT_NORSK)
}
