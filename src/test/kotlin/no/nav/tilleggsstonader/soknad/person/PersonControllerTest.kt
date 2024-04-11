package no.nav.tilleggsstonader.soknad.person

import io.mockk.every
import io.mockk.verify
import no.nav.tilleggsstonader.libs.test.fnr.FnrGenerator
import no.nav.tilleggsstonader.libs.utils.fnr.Fødselsnummer
import no.nav.tilleggsstonader.soknad.IntegrationTest
import no.nav.tilleggsstonader.soknad.infrastruktur.PdlClientConfig.Companion.resetPdlClientMock
import no.nav.tilleggsstonader.soknad.infrastruktur.lagPdlBarn
import no.nav.tilleggsstonader.soknad.infrastruktur.lagPdlSøker
import no.nav.tilleggsstonader.soknad.person.dto.PersonMedBarnDto
import no.nav.tilleggsstonader.soknad.person.pdl.PdlClientCredentialClient
import no.nav.tilleggsstonader.soknad.person.pdl.dto.AdressebeskyttelseGradering
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.exchange
import java.time.LocalDate

class PersonControllerTest : IntegrationTest() {

    @Autowired
    lateinit var pdlClientCredentialClient: PdlClientCredentialClient

    val personident = FnrGenerator.generer(LocalDate.of(2000, 1, 1))
    val barnident = FnrGenerator.generer(LocalDate.of(2020, 1, 1))

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(søkerBearerToken(personident))
    }

    @AfterEach
    override fun tearDown() {
        resetPdlClientMock(pdlClient)
        resetPdlClientMock(pdlClientCredentialClient)
    }

    @Test
    fun `skal feile hvis man ikke har med token`() {
        headers.remove("Authorization")

        assertThatThrownBy {
            hentPerson()
        }.hasMessage("""401 : "{"type":"about:blank","title":"Unauthorized","status":401,"detail":"Ukjent feil","instance":"/api/person"}"""")
    }

    @Test
    fun `skal kunne hente person fra pdl`() {
        val response = hentPerson()

        verify(exactly = 1) { pdlClient.hentSøker(Fødselsnummer(personident)) }
        assertThat(response.body!!.visningsnavn).isEqualTo("fornavn etternavn")
    }

    @Nested
    inner class GradertSøkerEllerBarn {

        private val expectedResponse =
            """400 : "{"type":"about:blank","title":"Bad Request","status":400,"detail":"ROUTING_GAMMEL_SØKNAD","instance":"/api/person"}""""

        @Test
        fun `skal kaste 400 i tilfelle søker er strengt fortrolig`() {
            every { pdlClient.hentSøker(any()) } returns
                lagPdlSøker(adressebeskyttelse = AdressebeskyttelseGradering.STRENGT_FORTROLIG)

            assertThatThrownBy { hentPerson() }.hasMessage(expectedResponse)
        }

        @Test
        fun `skal kaste 400 i tilfelle barn er strengt fortrolig`() {
            every { pdlClientCredentialClient.hentBarn(any()) } returns
                mapOf(lagPdlBarn(barnident, adressebeskyttelse = AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND))

            assertThatThrownBy { hentPerson() }.hasMessage(expectedResponse)
        }
    }

    private fun hentPerson(): ResponseEntity<PersonMedBarnDto> {
        val url = localhost("api/person")
        return restTemplate.exchange<PersonMedBarnDto>(url, HttpMethod.GET, HttpEntity(null, headers))
    }
}
