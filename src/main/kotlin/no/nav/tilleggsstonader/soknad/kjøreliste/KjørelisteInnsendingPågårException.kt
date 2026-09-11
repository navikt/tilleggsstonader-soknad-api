package no.nav.tilleggsstonader.soknad.kjøreliste

class KjørelisteInnsendingPågårException(
    cause: Throwable,
) : RuntimeException("En kjøreliste er allerede under innsending. Vent litt og prøv igjen.", cause)
