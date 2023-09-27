package no.nav.tilleggsstonader.soknad.dokument.pdf

import no.nav.tilleggsstonader.kontrakter.felles.Språkkode
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFelt
import no.nav.tilleggsstonader.kontrakter.søknad.TekstFelt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.AktivitetAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.BarnAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.BarnMedBarnepass
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.HovedytelseAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.SøknadsskjemaBarnetilsyn
import no.nav.tilleggsstonader.soknad.dokument.pdf.Feltformaterer.mapVerdi
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

sealed class HtmlVerdi
data class Verdiliste(
    val label: String,
    val verdiliste: List<HtmlVerdi>,
) : HtmlVerdi()

data class Verdi(
    val verdi: String,
) : HtmlVerdi()

object SøknadTreeWalker {

    fun mapSøknad(
        søknad: SøknadsskjemaBarnetilsyn,
        vedleggTitler: List<String>,
        språk: Språkkode,
    ): Verdiliste {
        val finnFelter = finnFelter(søknad, språk)
        val vedlegg = feltlisteMap("Vedlegg", listOf(Feltformaterer.mapVedlegg(vedleggTitler)))
        return feltlisteMap("Søknad om barnetilsyn <brevkode>", finnFelter + vedlegg)
    }

    /**
     * Avsnitt i skjemat har ikke labels, de definieres her
     */
    private val avsnittSpråkmapper = mapOf<KClass<*>, Map<Språkkode, String>>(
        HovedytelseAvsnitt::class to mapOf(Språkkode.NB to "Hovedytelse"),
        AktivitetAvsnitt::class to mapOf(Språkkode.NB to "Aktivitet"),
        BarnAvsnitt::class to mapOf(Språkkode.NB to "Barn"),
    )

    private fun tittelAvsnitt(kClass: Any, språk: Språkkode): String =
        avsnittSpråkmapper[kClass::class]?.get(språk)
            ?: error("Finner ikke språkmapping for ${kClass::class.java.simpleName}-$språk")

    /**
     * For å ha litt mer kontroll så må alle typer definieres
     */
    private fun finnFelter(entitet: Any, språk: Språkkode): List<Verdiliste> {
        return when (entitet) {
            is SøknadsskjemaBarnetilsyn,
            is BarnMedBarnepass,
            -> finnFelter(finnParametere(entitet), språk)

            is HovedytelseAvsnitt,
            is AktivitetAvsnitt,
            is BarnAvsnitt,
            ->
                listOf(Verdiliste(tittelAvsnitt(entitet, språk), finnFelter(finnParametere(entitet), språk)))

            is List<Any?> -> entitet.filterNotNull().map { finnFelter(it, språk) }.flatten()
            is TekstFelt -> listOf(feltlisteMap(entitet.label, listOf(Verdi(mapVerdi(entitet.verdi)))))
            is EnumFelt<*> -> listOf(feltlisteMap(entitet.label, listOf(Verdi(mapVerdi(entitet.svarTekst)))))
            else -> error("Kan ikke mappe entitet=$entitet")
        }
    }

    private data class SpecialHåndtering<T : Any, OUT : Any>(
        val kClass: KClass<T>,
        val kProperty1: KProperty1<T, OUT>,
        val mapper: (verdi: OUT) -> Any,
    )

    // Hvis det skulle bli behov for specialhåndtering
    // eks SpecialHåndtering(BarnMedBarnepass::class, BarnMedBarnepass::ident) { TekstFelt("Fødselsnummer", "", it) },
    private val specialHåndtering = setOf<SpecialHåndtering<*, *>>().associateBy { Pair(it.kClass, it.kProperty1) }

    private fun finnParametere(entitet: Any): List<Any> {
        return konstruktørparametere(entitet)
            .asSequence()
            .map { finnSøknadsfelt(entitet, it) }
            .filter { it.visibility == KVisibility.PUBLIC }
            .mapNotNull {
                val feltverdi = getFeltverdi(it, entitet)
                val parametere = specialHåndtering[Pair(entitet::class, it)]
                if (parametere != null && feltverdi != null) {
                    @Suppress("UNCHECKED_CAST")
                    val mapper = parametere.mapper as (verdi: Any) -> Map<String, Any>
                    mapper.invoke(feltverdi)
                } else {
                    feltverdi
                }
            }
            .toList()
    }

    private fun feltlisteMap(label: String, verdi: List<HtmlVerdi>) = Verdiliste(label, verdi)

    /**
     * Henter ut verdien for felt på entitet.
     */
    private fun getFeltverdi(felt: KProperty1<out Any, Any?>, entitet: Any) =
        felt.getter.call(entitet)

    /**
     * Finn første (og eneste) felt på entiteten som har samme navn som konstruktørparameter.
     */
    private fun finnSøknadsfelt(entity: Any, konstruktørparameter: KParameter) =
        entity::class.declaredMemberProperties.first { it.name == konstruktørparameter.name }

    /**
     * Konstruktørparametere er det eneste som gir oss en garantert rekkefølge for feltene, så vi henter disse først.
     */
    private fun konstruktørparametere(entity: Any) = entity::class.primaryConstructor?.parameters ?: emptyList()
}
