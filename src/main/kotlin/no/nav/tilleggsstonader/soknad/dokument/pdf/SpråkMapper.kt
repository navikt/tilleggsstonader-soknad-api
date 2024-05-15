package no.nav.tilleggsstonader.soknad.dokument.pdf

import no.nav.tilleggsstonader.kontrakter.felles.Språkkode
import no.nav.tilleggsstonader.kontrakter.søknad.Søknadsskjema
import no.nav.tilleggsstonader.kontrakter.søknad.SøknadsskjemaBarnetilsyn
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.AktivitetAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.ArbeidOgOpphold
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.BarnAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.HovedytelseAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.OppholdUtenforNorge
import kotlin.reflect.KClass

object SpråkMapper {

    fun tittelSøknadsskjema(søknad: Søknadsskjema<*>): String {
        val kClass = søknad.skjema::class
        val språk = søknad.språk
        return tittelSøknadsskjemaMapper[kClass]?.get(språk)
            ?: error("Finner ikke språkmapping for ${kClass::class.java.simpleName}-$språk")
    }

    fun tittelAvsnitt(kClass: Any, språk: Språkkode): String = avsnittSpråkmapper[kClass::class]?.get(språk)
        ?: error("Finner ikke språkmapping for ${kClass::class.java.simpleName}-$språk")

    private val tittelSøknadsskjemaMapper = mapOf<KClass<*>, Map<Språkkode, String>>(
        SøknadsskjemaBarnetilsyn::class to mapOf(
            Språkkode.NB to "Søknad om støtte til pass av barn",
        ),
    )

    /**
     * Avsnitt i skjemat har ikke labels, de definieres her
     */
    private val avsnittSpråkmapper = mapOf<KClass<*>, Map<Språkkode, String>>(
        HovedytelseAvsnitt::class to mapOf(Språkkode.NB to "Hovedytelse"),
        ArbeidOgOpphold::class to mapOf(Språkkode.NB to "Arbeid og opphold"),
        OppholdUtenforNorge::class to mapOf(Språkkode.NB to "Arbeid og opphold"),
        AktivitetAvsnitt::class to mapOf(Språkkode.NB to "Aktivitet"),
        BarnAvsnitt::class to mapOf(Språkkode.NB to "Barn"),
    )

    fun tittelOppholdUtenforNorgeSiste12mnd(språk: Språkkode) = when (språk) {
        Språkkode.NB -> "Opphold utenfor Norge siste 12 mnd"
        else -> error("Mangler mapping av $språk")
    }

    fun tittelOppholdUtenforNorgeNeste12mnd(språk: Språkkode) = when (språk) {
        Språkkode.NB -> "Opphold utenfor Norge neste 12 mnd"
        else -> error("Mangler mapping av $språk")
    }

    fun tittelAlternativer(språk: Språkkode) = when (språk) {
        Språkkode.NB -> "Alternativer"
        else -> error("Mangler mapping av $språk")
    }
}
