package no.nav.tilleggsstonader.soknad.dokument.pdf

import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.SøknadsskjemaBarnetilsyn
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeFormatter
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

object SøknadTreeWalker {
/*
    private val endNodes =
        setOf<KClass<*>>(
            String::class,
            Int::class,
            Boolean::class,
            Double::class,
            LocalDate::class,
            LocalDateTime::class,
            Month::class,
            Long::class,
        )

    fun mapOvergangsstønad(
        søknad: SøknadsskjemaBarnetilsyn,
        vedleggTitler: List<String>,
    ): Map<String, Any> {
        val finnFelter = finnFelter(søknad)
        val vedlegg = feltlisteMap("Vedlegg", listOf(Feltformaterer.mapVedlegg(vedleggTitler)))
        return feltlisteMap("Søknad om overgangsstønad (NAV 15-00.01)", finnFelter + vedlegg)
    }

    private fun finnFelter(entitet: Any): List<Map<String, *>> {
        // Det går ikke å hente elementene i en liste med reflection, så vi traverserer den som vanlig.
        if (entitet is List<Any?>) {
            return entitet.filterNotNull()
                .map { finnFelter(it) }
                .flatten()
        }
        val parametere = konstruktørparametere(entitet)

        val list = parametere
            .asSequence()
            .map { finnSøknadsfelt(entitet, it) }
            .filter { it.visibility == KVisibility.PUBLIC }
            .mapNotNull { getFeltverdi(it, entitet) }
            .map { finnFelter(it) } // Kall rekursivt videre
            .flatten()
            .toList()

        if (entitet is Søknadsfelt<*>) {
            if (entitet.verdi!! is Dokumentasjon) {
                @Suppress("UNCHECKED_CAST")
                return listOf(mapDokumentasjon(entitet as Søknadsfelt<Dokumentasjon>))
            }
            if (entitet.verdi!!::class in endNodes) {
                return listOf(Feltformaterer.mapEndenodeTilUtskriftMap(entitet))
            }
            if (entitet.verdi is List<*>) {
                val verdiliste = entitet.verdi as List<*>
                if (verdiliste.isNotEmpty() && verdiliste.first() is String) {
                    return listOf(Feltformaterer.mapEndenodeTilUtskriftMap(entitet))
                }
            }
            return listOf(feltlisteMap(entitet.label, list))
        }
        return list
    }

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

 */
}
