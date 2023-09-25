package no.nav.tilleggsstonader.soknad.soknad.barnetilsyn

import no.nav.tilleggsstonader.kontrakter.søknad.BooleanFelt
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFelt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.TypeBarnepass
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.ÅrsakBarnepass
import no.nav.tilleggsstonader.soknad.soknad.SøknadValideringException
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SøknadBarnetilsynDtoTest {

    @Nested
    inner class BarnMedBarnepassTest {
        val årsak = EnumFelt("", ÅrsakBarnepass.MYE_BORTE_ELLER_UVANLIG_ARBEIDSTID, "")

        @Test
        fun `må velge årsak hvis barnet har startet i 5e klasse`() {
            assertDoesNotThrow {
                barnMedBarnepass(startetIFemteklasse = true, årsak = årsak)
            }

            assertThatThrownBy {
                barnMedBarnepass(startetIFemteklasse = true, årsak = null)
            }.isInstanceOf(SøknadValideringException::class.java)
                .hasMessage("Må ha valgt årsak hvis barnet har begynt i 5. klasse")
        }

        @Test
        fun `skal ikke ha valgt årsak hvis man ikke startet i 5e klasse`() {
            assertDoesNotThrow {
                barnMedBarnepass(startetIFemteklasse = false, årsak = null)
            }

            assertThatThrownBy {
                barnMedBarnepass(startetIFemteklasse = false, årsak = årsak)
            }.isInstanceOf(SøknadValideringException::class.java)
                .hasMessage("Kan ikke sende inn årsak når barnet har begynt i 5. klasse")
        }

        private fun barnMedBarnepass(
            startetIFemteklasse: Boolean,
            årsak: EnumFelt<ÅrsakBarnepass>?,
        ) {
            BarnMedBarnepass(
                personIdent = "ident",
                type = EnumFelt("", TypeBarnepass.BARNEHAGE_SFO_AKS, ""),
                startetIFemte = BooleanFelt("", startetIFemteklasse),
                årsak = årsak,
            )
        }
    }
}
