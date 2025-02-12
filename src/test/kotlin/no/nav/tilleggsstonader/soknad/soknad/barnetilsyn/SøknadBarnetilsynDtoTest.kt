package no.nav.tilleggsstonader.soknad.soknad.barnetilsyn

import no.nav.tilleggsstonader.kontrakter.søknad.EnumFelt
import no.nav.tilleggsstonader.kontrakter.søknad.JaNei
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
        val årsak = EnumFelt("", ÅrsakBarnepass.MYE_BORTE_ELLER_UVANLIG_ARBEIDSTID, "", emptyList())

        @Test
        fun `må velge årsak dersom barnet har startet i 5 klasse`() {
            assertDoesNotThrow {
                barnMedBarnepass(startetIFemteklasse = JaNei.JA, årsak = årsak)
            }

            assertThatThrownBy {
                barnMedBarnepass(startetIFemteklasse = JaNei.JA, årsak = null)
            }.isInstanceOf(SøknadValideringException::class.java)
                .hasMessage("Må ha valgt årsak hvis barnet har begynt i 5. klasse")
        }

        @Test
        fun `skal ikke ha valgt årsak dersom barnet ikke har startet i 5 klasse`() {
            assertDoesNotThrow {
                barnMedBarnepass(startetIFemteklasse = JaNei.NEI, årsak = null)
            }

            assertThatThrownBy {
                barnMedBarnepass(startetIFemteklasse = JaNei.NEI, årsak = årsak)
            }.isInstanceOf(SøknadValideringException::class.java)
                .hasMessage("Kan ikke sende inn årsak når barnet ikke har begynt i 5. klasse")

            assertThatThrownBy {
                barnMedBarnepass(startetIFemteklasse = null, årsak = årsak)
            }.isInstanceOf(SøknadValideringException::class.java)
                .hasMessage("Kan ikke sende inn årsak når barnet ikke har begynt i 5. klasse")
        }

        private fun barnMedBarnepass(
            startetIFemteklasse: JaNei?,
            årsak: EnumFelt<ÅrsakBarnepass>?,
        ) {
            BarnMedBarnepass(
                ident = "ident",
                type = EnumFelt("", TypeBarnepass.BARNEHAGE_SFO_AKS, "", emptyList()),
                startetIFemte = startetIFemteklasse?.let { EnumFelt("", startetIFemteklasse, "", emptyList()) },
                årsak = årsak,
            )
        }
    }
}
