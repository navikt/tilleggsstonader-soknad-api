package no.nav.tilleggsstonader.soknad.dokument.pdf

import no.nav.tilleggsstonader.kontrakter.felles.Språkkode

data class Søkerinformasjon(
    val ident: String,
    val navn: String,
)

fun Søkerinformasjon.tilAvsnitt(språk: Språkkode) = Avsnitt(
    label = tittelAvsnitt(språk),
    verdier = listOf(
        Avsnitt(labelFødselsnummer(språk), listOf(Verdi(this.ident))),
        Avsnitt(labelNavn(språk), listOf(Verdi(this.navn))),
    ),
)

private fun tittelAvsnitt(språk: Språkkode) = when (språk) {
    Språkkode.NB -> "Søker"
    else -> error("Mangler mapping av $språk")
}
private fun labelFødselsnummer(språk: Språkkode) = when (språk) {
    Språkkode.NB -> "Fødselsnummer"
    else -> error("Mangler mapping av $språk")
}

private fun labelNavn(språk: Språkkode) = when (språk) {
    Språkkode.NB -> "Navn"
    else -> error("Mangler mapping av $språk")
}
