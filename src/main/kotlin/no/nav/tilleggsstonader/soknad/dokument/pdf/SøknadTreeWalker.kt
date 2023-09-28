package no.nav.tilleggsstonader.soknad.dokument.pdf

import no.nav.tilleggsstonader.kontrakter.felles.Språkkode
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFelt
import no.nav.tilleggsstonader.kontrakter.søknad.Søknadsskjema
import no.nav.tilleggsstonader.kontrakter.søknad.TekstFelt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.AktivitetAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.BarnAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.BarnMedBarnepass
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.HovedytelseAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.SøknadsskjemaBarnetilsyn
import no.nav.tilleggsstonader.soknad.dokument.pdf.Feltformaterer.mapVerdi
import no.nav.tilleggsstonader.soknad.dokument.pdf.SpråkMapper.tittelAvsnitt
import no.nav.tilleggsstonader.soknad.dokument.pdf.SpråkMapper.tittelSøknadsskjema
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * [SøknadTreeWalker] itererer over en søknad og genererer en struktur som brukes for å genere Html
 * [Avsnitt] skaper inline og verdiene itereres over og renderes
 * [Verdi] brukes For [EnumFelt], [TekstFelt] etc for å plukke ut selve verdiet og vise det frem i html'en
 * [HorisontalLinje] brukes i eks tilfeller der man har en liste med Barn, og lager en linje mellom hvert barn
 */
sealed class HtmlFelt
data class Avsnitt(
    val label: String,
    val verdier: List<HtmlFelt>,
) : HtmlFelt()

data class Verdi(
    val verdi: String,
) : HtmlFelt()

data object HorisontalLinje : HtmlFelt()

object SøknadTreeWalker {

    fun mapSøknad(
        søknad: Søknadsskjema<*>,
        vedleggTitler: List<String>,
    ): Avsnitt {
        val finnFelter = mapFelter(søknad.skjema, søknad.språk)
        val vedlegg = Avsnitt("Vedlegg", listOf(Feltformaterer.mapVedlegg(vedleggTitler)))
        return Avsnitt(tittelSøknadsskjema(søknad), finnFelter + vedlegg)
    }

    /**
     * For å ha litt mer kontroll så må alle typer definieres
     */
    private fun mapFelter(entitet: Any?, språk: Språkkode): List<HtmlFelt> {
        return when (entitet) {
            is SøknadsskjemaBarnetilsyn,
            is BarnMedBarnepass,
            -> finnFelter(entitet, språk)

            is HovedytelseAvsnitt,
            is AktivitetAvsnitt,
            is BarnAvsnitt,
            -> listOf(Avsnitt(tittelAvsnitt(entitet, språk), finnFelter(entitet, språk)))

            is List<*> -> mapListe(entitet, språk)

            is TekstFelt -> listOf(Avsnitt(entitet.label, listOf(Verdi(mapVerdi(entitet.verdi)))))
            is EnumFelt<*> -> listOf(Avsnitt(entitet.label, listOf(Verdi(mapVerdi(entitet.svarTekst)))))
            else -> error("Kan ikke mappe entitet=$entitet")
        }
    }

    /**
     * I de tilfeller man eks har en liste med Barn, så er det ønskelig å lage en Horisontallinje mellom barnen
     */
    private fun mapListe(
        entitet: List<*>,
        språk: Språkkode
    ) = entitet.filterNotNull().mapIndexed { index, it ->
        val felter = mapFelter(it, språk)
        if (index != 0) {
            listOf(HorisontalLinje) + felter
        } else {
            felter
        }
    }.flatten()

    private fun finnFelter(
        entitet: Any,
        språk: Språkkode,
    ) = finnParametere(entitet).map { mapFelter(it, språk) }.flatten()

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
