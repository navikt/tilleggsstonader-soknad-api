package no.nav.tilleggsstonader.soknad.infrastruktur.database

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

object SporbarUtils {
    fun now(): LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
}
