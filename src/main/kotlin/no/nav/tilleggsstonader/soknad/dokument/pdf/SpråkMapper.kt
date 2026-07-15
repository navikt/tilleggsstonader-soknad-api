package no.nav.tilleggsstonader.soknad.dokument.pdf

import no.nav.tilleggsstonader.kontrakter.felles.Språkkode
import no.nav.tilleggsstonader.kontrakter.søknad.InnsendtSkjema

object SpråkMapper {
    fun tittelSøknadsskjema(søknad: InnsendtSkjema<*>): String {
        val språk = søknad.språk

        return søknad.skjema.språkMapper()[språk]
            ?: error("Finner ikke språkmapping for ${søknad.skjema::class.java.simpleName}-$språk")
    }

    fun tittelSamlinger(språk: Språkkode) =
        when (språk) {
            Språkkode.NB -> "Samlinger"
            else -> error("Mangler mapping av $språk")
        }

    fun tittelOppholdUtenforNorgeSiste12mnd(språk: Språkkode) =
        when (språk) {
            Språkkode.NB -> "Opphold utenfor Norge siste 12 mnd"
            else -> error("Mangler mapping av $språk")
        }

    fun tittelOppholdUtenforNorgeNeste12mnd(språk: Språkkode) =
        when (språk) {
            Språkkode.NB -> "Opphold utenfor Norge neste 12 mnd"
            else -> error("Mangler mapping av $språk")
        }

    fun tittelAlternativer(språk: Språkkode) =
        when (språk) {
            Språkkode.NB -> "Alternativer"
            else -> error("Mangler mapping av $språk")
        }
}
