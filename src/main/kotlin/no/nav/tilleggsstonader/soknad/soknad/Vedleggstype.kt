package no.nav.tilleggsstonader.soknad.soknad

/**
 * @param tittel brukes som tittel for vedlegget i dokarkiv, sånn at det vises i gosys
 */
enum class Vedleggstype(val tittel: String) {
    EKSEMPEL("Eksempel") // Denne kan slettes, kun brukt for å sette opp tester
}