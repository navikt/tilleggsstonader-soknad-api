package no.nav.tilleggsstonader.soknad.util

import java.time.format.DateTimeFormatter

object DatoFormat {

    val DATE_FORMAT_NORSK = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val DATE_TIME_FORMAT_NORSK = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
}