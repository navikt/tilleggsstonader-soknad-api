package no.nav.tilleggsstonader.soknad.dokument.pdf

import no.nav.tilleggsstonader.kontrakter.felles.Språkkode
import no.nav.tilleggsstonader.kontrakter.søknad.InnsendtSkjema
import no.nav.tilleggsstonader.kontrakter.søknad.KjørelisteSkjema
import no.nav.tilleggsstonader.kontrakter.søknad.SøknadsskjemaBarnetilsyn
import no.nav.tilleggsstonader.kontrakter.søknad.SøknadsskjemaLæremidler
import no.nav.tilleggsstonader.kontrakter.søknad.SøknadsskjemaReiseTilSamling
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.AktivitetAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.BarnAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.felles.ArbeidOgOpphold
import no.nav.tilleggsstonader.kontrakter.søknad.felles.HovedytelseAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.felles.OppholdUtenforNorge
import no.nav.tilleggsstonader.kontrakter.søknad.læremidler.UtdanningAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.ReiseavstandAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.ReisemåteAvsnitt
import kotlin.reflect.KClass
import no.nav.tilleggsstonader.kontrakter.søknad.reisetilsamling.AktivitetAvsnitt as ReiseTilSamlingAktivitetAvsnitt

object SpråkMapper {
    fun tittelSøknadsskjema(søknad: InnsendtSkjema<*>): String {
        val kClass = søknad.skjema::class
        val språk = søknad.språk
        return tittelSøknadsskjemaMapper[kClass]?.get(språk)
            ?: error("Finner ikke språkmapping for ${kClass::class.java.simpleName}-$språk")
    }

    fun tittelAvsnitt(
        kClass: Any,
        språk: Språkkode,
    ): String =
        avsnittSpråkmapper[kClass::class]?.get(språk)
            ?: error("Finner ikke språkmapping for ${kClass::class.java.simpleName}-$språk")

    private val tittelSøknadsskjemaMapper =
        mapOf<KClass<*>, Map<Språkkode, String>>(
            SøknadsskjemaBarnetilsyn::class to
                mapOf(
                    Språkkode.NB to "Søknad om støtte til pass av barn",
                ),
            SøknadsskjemaLæremidler::class to
                mapOf(
                    Språkkode.NB to "Søknad om støtte til læremidler",
                ),
            KjørelisteSkjema::class to
                mapOf(
                    Språkkode.NB to "Refusjon av utgifter til daglig reise med bruk av bil",
                ),
            SøknadsskjemaReiseTilSamling::class to
                mapOf(
                    Språkkode.NB to "Søknad om støtte til reise til samling",
                ),
        )

    /**
     * Avsnitt i skjemat har ikke labels, de definieres her
     */
    private val avsnittSpråkmapper =
        mapOf(
            HovedytelseAvsnitt::class to mapOf(Språkkode.NB to "Hovedytelse"),
            ArbeidOgOpphold::class to mapOf(Språkkode.NB to "Arbeid og opphold"),
            OppholdUtenforNorge::class to mapOf(Språkkode.NB to "Arbeid og opphold"),
            AktivitetAvsnitt::class to mapOf(Språkkode.NB to "Aktivitet"),
            BarnAvsnitt::class to mapOf(Språkkode.NB to "Barn"),
            UtdanningAvsnitt::class to mapOf(Språkkode.NB to "Utdanning"),
            ReiseTilSamlingAktivitetAvsnitt::class to mapOf(Språkkode.NB to "Aktivitet"),
            ReiseavstandAvsnitt::class to mapOf(Språkkode.NB to "Reiseavstand"),
            ReisemåteAvsnitt::class to mapOf(Språkkode.NB to "Reisemåte"),
        )

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
