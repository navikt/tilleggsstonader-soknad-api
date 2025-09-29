package no.nav.tilleggsstonader.soknad.kjøreliste

import java.time.LocalDateTime

data class KjørelisteResponse(
    val mottattTidspunkt: LocalDateTime,
    val saksnummer: Int,
)
