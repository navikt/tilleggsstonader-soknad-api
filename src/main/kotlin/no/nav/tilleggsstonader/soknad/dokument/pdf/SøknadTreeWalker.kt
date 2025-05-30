package no.nav.tilleggsstonader.soknad.dokument.pdf

import no.nav.tilleggsstonader.kontrakter.felles.Språkkode
import no.nav.tilleggsstonader.kontrakter.søknad.DatoFelt
import no.nav.tilleggsstonader.kontrakter.søknad.DokumentasjonFelt
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFelt
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFlereValgFelt
import no.nav.tilleggsstonader.kontrakter.søknad.SelectFelt
import no.nav.tilleggsstonader.kontrakter.søknad.Søknadsskjema
import no.nav.tilleggsstonader.kontrakter.søknad.SøknadsskjemaBarnetilsyn
import no.nav.tilleggsstonader.kontrakter.søknad.SøknadsskjemaLæremidler
import no.nav.tilleggsstonader.kontrakter.søknad.TekstFelt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.AktivitetAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.BarnAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.BarnMedBarnepass
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.Utgifter
import no.nav.tilleggsstonader.kontrakter.søknad.felles.ArbeidOgOpphold
import no.nav.tilleggsstonader.kontrakter.søknad.felles.HovedytelseAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.felles.OppholdUtenforNorge
import no.nav.tilleggsstonader.kontrakter.søknad.læremidler.HarRettTilUtstyrsstipend
import no.nav.tilleggsstonader.kontrakter.søknad.læremidler.UtdanningAvsnitt
import no.nav.tilleggsstonader.soknad.dokument.pdf.Feltformaterer.mapVerdi
import no.nav.tilleggsstonader.soknad.dokument.pdf.SpråkMapper.tittelAlternativer
import no.nav.tilleggsstonader.soknad.dokument.pdf.SpråkMapper.tittelAvsnitt
import no.nav.tilleggsstonader.soknad.dokument.pdf.SpråkMapper.tittelOppholdUtenforNorgeNeste12mnd
import no.nav.tilleggsstonader.soknad.dokument.pdf.SpråkMapper.tittelOppholdUtenforNorgeSiste12mnd
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
sealed class HtmlFelt(
    val type: HtmlFeltType,
)

enum class HtmlFeltType {
    AVSNITT,
    VERDI,
    LINJE,
}

data class Avsnitt(
    val label: String,
    val verdier: List<HtmlFelt>,
) : HtmlFelt(HtmlFeltType.AVSNITT)

data class Verdi(
    val verdi: String,
    val alternativer: String? = null,
) : HtmlFelt(HtmlFeltType.VERDI)

data object HorisontalLinje : HtmlFelt(HtmlFeltType.LINJE)

object SøknadTreeWalker {
    fun mapSøknad(
        søknad: Søknadsskjema<*>,
        søkerinformasjon: Søkerinformasjon,
    ): List<HtmlFelt> = listOf(søkerinformasjon.tilAvsnitt(søknad.språk)) + mapFelter(søknad.skjema, søknad.språk)

    /**
     * For å ha litt mer kontroll så må alle typer definieres
     */
    private fun mapFelter(
        entitet: Any?,
        språk: Språkkode,
    ): List<HtmlFelt> =
        when (entitet) {
            is SøknadsskjemaBarnetilsyn,
            is BarnMedBarnepass,
            is Utgifter,
            is OppholdUtenforNorge,
            is SøknadsskjemaLæremidler,
            is HarRettTilUtstyrsstipend,
            -> finnFelter(entitet, språk)
            is HovedytelseAvsnitt,
            is AktivitetAvsnitt,
            is BarnAvsnitt,
            is UtdanningAvsnitt,
            is ArbeidOgOpphold,
            -> listOf(Avsnitt(label = tittelAvsnitt(entitet, språk), verdier = finnFelter(entitet, språk)))

            is ListeMedTittel -> listOf(Avsnitt(entitet.tittel, mapListe(entitet.list, språk)))
            is List<*> -> mapListe(entitet, språk)

            is TekstFelt -> listOf(Avsnitt(entitet.label, listOf(Verdi(mapVerdi(entitet.verdi)))))
            is SelectFelt<*> -> listOf(Avsnitt(entitet.label, listOf(Verdi(mapVerdi(entitet.svarTekst)))))
            is DatoFelt -> listOf(Avsnitt(entitet.label, listOf(Verdi(mapVerdi(entitet.verdi)))))
            is EnumFelt<*> -> listOf(mapEnumFelt(entitet, språk))

            is EnumFlereValgFelt<*> -> listOf(mapEnumFlereValgFelt(entitet, språk))

            is DokumentasjonFelt -> emptyList()
            else -> error("Kan ikke mappe entitet=$entitet")
        }

    private fun mapEnumFlereValgFelt(
        entitet: EnumFlereValgFelt<*>,
        språk: Språkkode,
    ) = Avsnitt(
        label = entitet.label,
        verdier =
            listOf(
                Verdi(
                    verdi = mapVerdi(entitet.verdier.map { it.label }),
                    alternativer = mapAlternativer(entitet.alternativer, språk),
                ),
            ),
    )

    private fun mapEnumFelt(
        entitet: EnumFelt<*>,
        språk: Språkkode,
    ) = Avsnitt(
        entitet.label,
        listOf(
            Verdi(
                mapVerdi(entitet.svarTekst),
                alternativer = mapAlternativer(entitet.alternativer, språk),
            ),
        ),
    )

    private fun mapAlternativer(
        alternativer: List<String>,
        språk: Språkkode,
    ): String? = alternativer.takeIf { it.isNotEmpty() }?.let { "${tittelAlternativer(språk)}: ${it.joinToString(", ")}" }

    /**
     * I de tilfeller man eks har en liste med Barn, så er det ønskelig å lage en Horisontallinje mellom barnen
     */
    private fun mapListe(
        entitet: List<*>,
        språk: Språkkode,
    ): List<HtmlFelt> =
        entitet
            .filterNotNull()
            .mapIndexed { index, it ->
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
    ) = finnParametere(entitet, språk).map { mapFelter(it, språk) }.flatten()

    private data class SpecialHåndtering<T : Any, OUT : Any>(
        val kClass: KClass<T>,
        val kProperty1: KProperty1<T, OUT>,
        val mapper: (verdi: OUT, språk: Språkkode) -> Any,
    )

    // Hvis det skulle bli behov for specialhåndtering
    // eks SpecialHåndtering(BarnMedBarnepass::class, BarnMedBarnepass::ident) { TekstFelt("Fødselsnummer", "", it) },
    private val specialHåndtering =
        setOf<SpecialHåndtering<*, *>>(
            SpecialHåndtering(ArbeidOgOpphold::class, ArbeidOgOpphold::oppholdUtenforNorgeSiste12mnd) { verdi, språk ->
                ListeMedTittel(tittelOppholdUtenforNorgeSiste12mnd(språk), verdi)
            },
            SpecialHåndtering(ArbeidOgOpphold::class, ArbeidOgOpphold::oppholdUtenforNorgeNeste12mnd) { verdi, språk ->
                ListeMedTittel(tittelOppholdUtenforNorgeNeste12mnd(språk), verdi)
            },
        ).associateBy { Pair(it.kClass, it.kProperty1) }

    private fun finnParametere(
        entitet: Any,
        språk: Språkkode,
    ): List<Any> =
        konstruktørparametere(entitet)
            .asSequence()
            .map { finnSøknadsfelt(entitet, it) }
            .filter { it.visibility == KVisibility.PUBLIC }
            .mapNotNull {
                val feltverdi = getFeltverdi(it, entitet)
                val parametere = specialHåndtering[Pair(entitet::class, it)]
                if (parametere != null && feltverdi != null) {
                    @Suppress("UNCHECKED_CAST")
                    val mapper =
                        parametere.mapper as (verdi: Any, språk: Språkkode) -> Map<String, Any>
                    mapper.invoke(feltverdi, språk)
                } else {
                    feltverdi
                }
            }.toList()

    /**
     * Henter ut verdien for felt på entitet.
     */
    private fun getFeltverdi(
        felt: KProperty1<out Any, Any?>,
        entitet: Any,
    ) = felt.getter.call(entitet)

    /**
     * Finn første (og eneste) felt på entiteten som har samme navn som konstruktørparameter.
     */
    private fun finnSøknadsfelt(
        entity: Any,
        konstruktørparameter: KParameter,
    ) = entity::class.declaredMemberProperties.first { it.name == konstruktørparameter.name }

    /**
     * Konstruktørparametere er det eneste som gir oss en garantert rekkefølge for feltene, så vi henter disse først.
     */
    private fun konstruktørparametere(entity: Any) = entity::class.primaryConstructor?.parameters ?: emptyList()

    private data class ListeMedTittel(
        val tittel: String,
        val list: List<*>,
    )
}
