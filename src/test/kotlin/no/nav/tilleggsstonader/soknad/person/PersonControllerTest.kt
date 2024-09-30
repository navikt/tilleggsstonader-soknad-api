package no.nav.tilleggsstonader.soknad.person

import io.mockk.verify
import no.nav.tilleggsstonader.libs.test.fnr.FnrGenerator
import no.nav.tilleggsstonader.libs.utils.fnr.Fødselsnummer
import no.nav.tilleggsstonader.soknad.IntegrationTest
import no.nav.tilleggsstonader.soknad.infrastruktur.PdlClientConfig.Companion.resetPdlClientMock
import no.nav.tilleggsstonader.soknad.person.dto.PersonMedBarnDto
import no.nav.tilleggsstonader.soknad.person.pdl.PdlClientCredentialClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
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
    fun `skal kunne hente person fra pdl uten å ta med barn`() {
        val response = hentPerson()

        verify(exactly = 1) { pdlClient.hentSøker(Fødselsnummer(personident)) }
        assertThat(response.body!!.visningsnavn).isEqualTo("fornavn etternavn")
        assertThat(response.body!!.barn).hasSize(0)
    }

    @Test
    fun `skal kunne hente person med barn fra pdl`() {
        val response = hentPersonMedBarn()

        verify(exactly = 1) { pdlClient.hentSøker(Fødselsnummer(personident)) }
        assertThat(response.body!!.visningsnavn).isEqualTo("fornavn etternavn")
        assertThat(response.body!!.barn).hasSize(2)
    }

    private fun hentPerson(): ResponseEntity<PersonMedBarnDto> {
        val url = localhost("api/person")
        return restTemplate.exchange<PersonMedBarnDto>(url, HttpMethod.GET, HttpEntity(null, headers))
    }

    private fun hentPersonMedBarn(): ResponseEntity<PersonMedBarnDto> {
        val url = localhost("api/person/med-barn")
        return restTemplate.exchange<PersonMedBarnDto>(url, HttpMethod.GET, HttpEntity(null, headers))
    }
}
