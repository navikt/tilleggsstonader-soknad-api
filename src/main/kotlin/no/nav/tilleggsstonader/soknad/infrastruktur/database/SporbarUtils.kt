package no.nav.tilleggsstonader.soknad.infrastruktur.database

import no.nav.tilleggsstonader.libs.utils.osloNow
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

object SporbarUtils {

    fun now(): LocalDateTime = osloNow().truncatedTo(ChronoUnit.MILLIS)
}
