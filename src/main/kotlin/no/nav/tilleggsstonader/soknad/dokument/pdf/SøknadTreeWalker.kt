package no.nav.tilleggsstonader.soknad.dokument.pdf

import no.nav.tilleggsstonader.kontrakter.søknad.EnumFelt
import no.nav.tilleggsstonader.kontrakter.søknad.TekstFelt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.Aktivitet
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.BarnMedBarnepass
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.SøknadsskjemaBarnetilsyn
import no.nav.tilleggsstonader.soknad.dokument.pdf.Feltformaterer.mapVerdi
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

object SøknadTreeWalker {

    fun mapSøknad(
        søknad: SøknadsskjemaBarnetilsyn,
        vedleggTitler: List<String>,
    ): Map<String, Any> {
        val finnFelter = finnFelter(søknad)
        val vedlegg = feltlisteMap("Vedlegg", listOf(Feltformaterer.mapVedlegg(vedleggTitler)))
        return feltlisteMap("Søknad om barnetilsyn <brevkode>", finnFelter + vedlegg)
    }

    private fun finnFelter(entitet: Any): List<Map<String, *>> {
        return when (entitet) {
            is SøknadsskjemaBarnetilsyn,
            is Aktivitet,
            is BarnMedBarnepass,
            -> finnFelter(finnParametere(entitet))

            is List<Any?> -> entitet.filterNotNull().map { finnFelter(it) }.flatten()
            is TekstFelt -> listOf(feltlisteMap(entitet.label, listOf(mapVerdi(entitet.verdi))))
            is EnumFelt<*> -> listOf(feltlisteMap(entitet.label, listOf(mapVerdi(entitet.svarTekst))))
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

    private fun feltlisteMap(label: String, verdi: String) = mapOf("label" to label, "verdiliste" to listOf(verdi))
    private fun feltlisteMap(label: String, verdi: List<*>) = mapOf("label" to label, "verdiliste" to verdi)

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
