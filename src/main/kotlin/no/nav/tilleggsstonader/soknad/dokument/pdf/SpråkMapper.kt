package no.nav.tilleggsstonader.soknad.dokument.pdf

import no.nav.tilleggsstonader.kontrakter.felles.Språkkode
import no.nav.tilleggsstonader.kontrakter.søknad.Søknadsskjema
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.AktivitetAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.BarnAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.DokumentasjonAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.HovedytelseAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.SøknadsskjemaBarnetilsyn
import kotlin.reflect.KClass

object SpråkMapper {

    fun tittelSøknadsskjema(søknad: Søknadsskjema<*>): String {
        val kClass = søknad.skjema!!::class
        val språk = søknad.språk
        return tittelSøknadsskjemaMapper[kClass]?.get(språk)
            ?: error("Finner ikke språkmapping for ${kClass::class.java.simpleName}-$språk")
    }

    fun tittelAvsnitt(kClass: Any, språk: Språkkode): String =
        avsnittSpråkmapper[kClass::class]?.get(språk)
            ?: error("Finner ikke språkmapping for ${kClass::class.java.simpleName}-$språk")

    private val tittelSøknadsskjemaMapper = mapOf<KClass<*>, Map<Språkkode, String>>(
        SøknadsskjemaBarnetilsyn::class to mapOf(Språkkode.NB to "Søknad om barnetilsyn <brevkode>"), // TODO brevkode
    )

    /**
     * Avsnitt i skjemat har ikke labels, de definieres her
     */
    private val avsnittSpråkmapper = mapOf<KClass<*>, Map<Språkkode, String>>(
        HovedytelseAvsnitt::class to mapOf(Språkkode.NB to "Hovedytelse"),
        AktivitetAvsnitt::class to mapOf(Språkkode.NB to "Aktivitet"),
        BarnAvsnitt::class to mapOf(Språkkode.NB to "Barn"),
        DokumentasjonAvsnitt::class to mapOf(Språkkode.NB to "Dokumentasjon")
    )
}
