package no.nav.tilleggsstonader.soknad.dokument.pdf

import no.nav.tilleggsstonader.kontrakter.felles.ObjectMapperProvider.objectMapper
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFelt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.TypeBarnepass
import no.nav.tilleggsstonader.soknad.soknad.SøknadTestUtil.lagInnsendtSkjema
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.BarnMedBarnepass
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.SøknadBarnetilsynUtil
import no.nav.tilleggsstonader.soknad.soknad.læremidler.SøknadLæremidlerUtil
import no.nav.tilleggsstonader.soknad.util.FileUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SøknadTreeWalkerTest {
    val søkerinformasjon = Søkerinformasjon("25518735813", "Fornavn Etternavn")

    @Nested
    inner class Barnetilsyn {
        @Test
        fun `skal mappe barnetilsyn`() {
            val søknadsskjema = lagInnsendtSkjema(SøknadBarnetilsynUtil.søknadBarnetilsyn)
            val htmlFelter = SøknadTreeWalker.mapSøknad(søknadsskjema, søkerinformasjon)
            assertExpected(
                "søknad/barnetilsyn/barnetilsyn_verdiliste.json",
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(htmlFelter),
            )
        }
    }

    @Nested
    inner class Læremidler {
        @Test
        fun `skal mappe læremidler`() {
            val søknadsskjema = lagInnsendtSkjema(SøknadLæremidlerUtil.søknadLæremidler)
            val htmlFelter = SøknadTreeWalker.mapSøknad(søknadsskjema, søkerinformasjon)
            assertExpected(
                "søknad/læremidler/læremidler_verdiliste.json",
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(htmlFelter),
            )
        }
    }

    @Test
    fun `skal håndtere felter med nullverdier`() {
        val barnMedBarnepass =
            BarnMedBarnepass(
                ident = "ident",
                type = EnumFelt("label", TypeBarnepass.BARNEHAGE_SFO_AKS, "svar", emptyList()),
                utgifter = null,
                startetIFemte = null,
                årsak = null,
            )
        val søknad =
            SøknadBarnetilsynUtil.søknadBarnetilsyn.copy(barnMedBarnepass = listOf(barnMedBarnepass), dokumentasjon = emptyList())
        val søknadsskjema = lagInnsendtSkjema(søknad)
        val result = SøknadTreeWalker.mapSøknad(søknadsskjema, søkerinformasjon)
        assertExpected(
            "søknad/barnetilsyn/barnetilsyn_verdiliste_nullverdier.json",
            objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result),
        )
    }

    private fun assertExpected(
        filnavn: String,
        actual: String,
    ) {
        // Kan brukes ved endringer for å skrive ny output til fil og sen verifisere
        FileUtil.skrivTilFil(filnavn, actual)
        assertThat(actual).isEqualToIgnoringWhitespace(FileUtil.readFile(filnavn))
    }
}
