package no.nav.tilleggsstonader.soknad.dokument.pdf

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object Feltformaterer {

    fun mapVedlegg(vedleggTitler: List<String>): Verdiliste {
        val verdi = vedleggTitler.joinToString("\n\n")
        return Verdiliste("Vedlegg", listOf(Verdi(verdi)))
    }

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
            is Collection<*> -> verdi.joinToString("\n\n") { mapVerdi(it!!) }
            is LocalDate -> tilUtskriftsformat(verdi)
            is LocalDateTime -> tilUtskriftsformat(verdi)
            else -> error("Kan ikke mappe $verdi")
        }
    }

    private fun tilUtskriftsformat(verdi: Boolean) = if (verdi) "Ja" else "Nei"
    private fun tilUtskriftsformat(verdi: Double) = String.format("%.2f", verdi).replace(".", ",")
    private fun tilUtskriftsformat(verdi: Month) = verdi.getDisplayName(TextStyle.FULL, Locale("no"))
    private fun tilUtskriftsformat(verdi: LocalDateTime) =
        verdi.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))

    private fun tilUtskriftsformat(verdi: LocalDate): String {
        return verdi.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    }

    fun feltMap(label: String, verdi: String, alternativer: List<String>? = null): Map<String, String> {
        return if (alternativer != null) {
            mapOf("label" to label, "verdi" to verdi, "alternativer" to alternativer.joinToString(" / "))
        } else {
            mapOf("label" to label, "verdi" to verdi)
        }
    }
}
