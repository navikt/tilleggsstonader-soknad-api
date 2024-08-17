package no.nav.tilleggsstonader.soknad.person

import io.mockk.every
import io.mockk.mockk
import no.nav.tilleggsstonader.libs.test.fnr.FnrGenerator
import no.nav.tilleggsstonader.libs.utils.fnr.Fødselsnummer
import no.nav.tilleggsstonader.soknad.infrastruktur.lagPdlBarn
import no.nav.tilleggsstonader.soknad.infrastruktur.lagPdlSøker
import no.nav.tilleggsstonader.soknad.person.pdl.PdlClient
import no.nav.tilleggsstonader.soknad.person.pdl.PdlClientCredentialClient
import no.nav.tilleggsstonader.soknad.person.pdl.dto.AdressebeskyttelseGradering
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlBarn
import org.assertj.core.api.Assertions.assertThat
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

        @Test
        fun `søker ugradert - skal kun inneholde barn som er ugradert`() {
            mockSøker(AdressebeskyttelseGradering.UGRADERT)

            assertForventedeBarn(ugradertBarn)
        }

        @Test
        fun `søker fortrolig - skal inneholde barn som har lik eller lavere gradering`() {
            mockSøker(AdressebeskyttelseGradering.FORTROLIG)

            assertForventedeBarn(ugradertBarn, fortroligBarn)
        }

        @Test
        fun `søker strengt fortrolig - skal inneholde barn som har lik eller lavere gradering`() {
            mockSøker(AdressebeskyttelseGradering.STRENGT_FORTROLIG)

            assertForventedeBarn(ugradertBarn, fortroligBarn, strengtFortroligBarn, strengtFortroligUtlandBarn)
        }

        private fun mockSøker(adressebeskyttelseGradering: AdressebeskyttelseGradering) {
            every { pdlClient.hentSøker(any()) } returns lagPdlSøker(adressebeskyttelse = adressebeskyttelseGradering)
        }

        private fun assertForventedeBarn(vararg forventedeBarn: Pair<String, PdlBarn>) {
            assertThat(service.hentSøker(identSøker).barn.map { it.ident })
                .containsAnyElementsOf(forventedeBarn.map { it.first })
        }
    }

    private fun lagBarn(gradering: AdressebeskyttelseGradering): Pair<String, PdlBarn> = lagPdlBarn(
        FnrGenerator.generer(),
        adressebeskyttelse = gradering,
    )
}
