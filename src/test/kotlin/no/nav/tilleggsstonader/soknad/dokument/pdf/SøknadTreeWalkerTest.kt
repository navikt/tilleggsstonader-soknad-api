package no.nav.tilleggsstonader.soknad.dokument.pdf

import no.nav.tilleggsstonader.kontrakter.felles.ObjectMapperProvider.objectMapper
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFelt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.TypeBarnepass
import no.nav.tilleggsstonader.soknad.soknad.SøknadTestUtil.lagSøknadsskjema
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.BarnMedBarnepass
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.BarnetilsynMapper
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.SøknadBarnetilsynUtil
import no.nav.tilleggsstonader.soknad.util.FileUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SøknadTreeWalkerTest {

    @Nested
    inner class Barnetilsyn {
        @Test
        fun `skal mappe barnetilsyn`() {
            val result = SøknadTreeWalker.mapSøknad(
                lagSøknadsskjema(BarnetilsynMapper().map(SøknadBarnetilsynUtil.søknad)),
                emptyList(),
            )
            assertExpected(
                "søknad/barnetilsyn_verdiliste.json",
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result),
            )
        }
    }

    @Test
    fun `skal håndtere felter med nullverdier`() {
        val barnMedBarnepass =
            BarnMedBarnepass(
                ident = "ident",
                type = EnumFelt("label", TypeBarnepass.BARNEHAGE_SFO_AKS, "svar"),
                startetIFemte = null,
                årsak = null,
            )
        val søknad = SøknadBarnetilsynUtil.søknad.copy(barn = listOf(barnMedBarnepass))
        val søknadsskjema = lagSøknadsskjema(BarnetilsynMapper().map(søknad))
        val result = SøknadTreeWalker.mapSøknad(søknadsskjema, emptyList())
        assertExpected(
            "søknad/barnetilsyn_verdiliste_nullverdier.json",
            objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result),
        )
    }

    private fun assertExpected(filnavn: String, actual: String) {
        // Kan brukes ved endringer for å skrive ny output til fil og sen verifisere
        // FileUtil.skrivTilFil(filnavn, actual)
        assertThat(actual).isEqualTo(FileUtil.readFile(filnavn))
    }
}
