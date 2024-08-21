package no.nav.tilleggsstonader.soknad.person

import io.mockk.every
import io.mockk.mockk
import no.nav.tilleggsstonader.libs.test.fnr.FnrGenerator
import no.nav.tilleggsstonader.libs.utils.fnr.Fødselsnummer
import no.nav.tilleggsstonader.soknad.infrastruktur.exception.GradertBrukerException
import no.nav.tilleggsstonader.soknad.infrastruktur.lagPdlBarn
import no.nav.tilleggsstonader.soknad.infrastruktur.lagPdlSøker
import no.nav.tilleggsstonader.soknad.person.dto.PersonMedBarnDto
import no.nav.tilleggsstonader.soknad.person.pdl.PdlClient
import no.nav.tilleggsstonader.soknad.person.pdl.PdlClientCredentialClient
import no.nav.tilleggsstonader.soknad.person.pdl.dto.AdressebeskyttelseGradering
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlBarn
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PersonServiceTest {

    private val pdlClient = mockk<PdlClient>()
    private val pdlClientCredentialClient = mockk<PdlClientCredentialClient>()
    private val adresseMapper = mockk<AdresseMapper>()

    private val service = PersonService(pdlClient, pdlClientCredentialClient, adresseMapper)

    private val identSøker = Fødselsnummer(FnrGenerator.generer())

    @BeforeEach
    fun setUp() {
        every { pdlClientCredentialClient.hentBarn(any()) } returns emptyMap()
        every { adresseMapper.tilFormatertAdresse(any()) } returns "Adresse 1"
    }

    @Nested
    inner class FiltreringAvBarnBasertPåGradering {

        val ugradertBarn = lagBarn(AdressebeskyttelseGradering.UGRADERT)
        val fortroligBarn = lagBarn(AdressebeskyttelseGradering.FORTROLIG)
        val strengtFortroligBarn = lagBarn(AdressebeskyttelseGradering.STRENGT_FORTROLIG)
        val strengtFortroligUtlandBarn = lagBarn(AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND)

        @BeforeEach
        fun setUp() {
            val barn = listOf(ugradertBarn, fortroligBarn, strengtFortroligBarn, strengtFortroligUtlandBarn).toMap()
            every { pdlClientCredentialClient.hentBarn(any()) } returns barn
        }

        @Nested
        inner class SøkerBarnGradering {

            @Test
            fun `søker ugradert med fortrolige barn skal kaste feil`() {
                mockSøker(AdressebeskyttelseGradering.UGRADERT)
                mockBarn(ugradertBarn, fortroligBarn)

                assertThatThrownBy {
                    service.hentSøker(identSøker)
                }.isInstanceOf(GradertBrukerException::class.java)
            }

            @Test
            fun `søker fortrolig med strengt fortrolig barn skal kaste feil`() {
                mockSøker(AdressebeskyttelseGradering.FORTROLIG)
                mockBarn(strengtFortroligBarn)

                assertThatThrownBy {
                    service.hentSøker(identSøker)
                }.isInstanceOf(GradertBrukerException::class.java)
            }

            @Test
            fun `søker fortrolig med fortrolig barn skal inneholde ugradert og fortrolig barn`() {
                mockSøker(AdressebeskyttelseGradering.FORTROLIG)
                mockBarn(ugradertBarn, fortroligBarn)

                val dto = service.hentSøker(identSøker)
                assertForventedeBarn(dto, ugradertBarn, fortroligBarn)
            }

            @Test
            fun `søker strengt fortrolig - skal ikke kaste feil uansett gradering på barn`() {
                mockSøker(AdressebeskyttelseGradering.STRENGT_FORTROLIG)
                mockBarn(ugradertBarn, fortroligBarn, strengtFortroligBarn, strengtFortroligUtlandBarn)

                val dto = service.hentSøker(identSøker)
                assertForventedeBarn(
                    dto,
                    ugradertBarn,
                    fortroligBarn,
                    strengtFortroligBarn,
                    strengtFortroligUtlandBarn,
                )
            }
        }

        private fun mockSøker(adressebeskyttelseGradering: AdressebeskyttelseGradering) {
            every { pdlClient.hentSøker(any()) } returns lagPdlSøker(adressebeskyttelse = adressebeskyttelseGradering)
        }

        private fun mockBarn(vararg barn: Pair<String, PdlBarn>) {
            every { pdlClientCredentialClient.hentBarn(any()) } returns barn.toMap()
        }

        private fun assertForventedeBarn(
            dto: PersonMedBarnDto,
            vararg forventedeBarn: Pair<String, PdlBarn>,
        ) {
            assertThat(dto.barn.map { it.ident })
                .containsAnyElementsOf(forventedeBarn.map { it.first })
        }
    }

    private fun lagBarn(gradering: AdressebeskyttelseGradering): Pair<String, PdlBarn> = lagPdlBarn(
        FnrGenerator.generer(),
        adressebeskyttelse = gradering,
    )
}
