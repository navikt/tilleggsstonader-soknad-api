package no.nav.tilleggsstonader.soknad.aktivitet

import no.nav.tilleggsstonader.kontrakter.felles.Skjematype

data class AktivitetRequest(
    val skjematype: Skjematype,
)
