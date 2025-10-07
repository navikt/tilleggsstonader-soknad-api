package no.nav.tilleggsstonader.soknad.person

import io.mockk.verify
import no.nav.tilleggsstonader.libs.test.fnr.FnrGenerator
import no.nav.tilleggsstonader.libs.utils.fnr.Fødselsnummer
import no.nav.tilleggsstonader.soknad.IntegrationTest
import no.nav.tilleggsstonader.soknad.infrastruktur.PdlClientConfig.Companion.resetPdlClientMock
import no.nav.tilleggsstonader.soknad.integrasjonstest.extensions.kall.hentPerson
import no.nav.tilleggsstonader.soknad.integrasjonstest.extensions.kall.hentPersonKall
import no.nav.tilleggsstonader.soknad.integrasjonstest.extensions.kall.hentPersonMedBarn
import no.nav.tilleggsstonader.soknad.person.pdl.PdlClientCredentialClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

class PersonControllerTest : IntegrationTest() {
    @Autowired
    lateinit var pdlClientCredentialClient: PdlClientCredentialClient

    val personident = FnrGenerator.generer(LocalDate.of(2000, 1, 1))

    @AfterEach
    override fun tearDown() {
        resetPdlClientMock(pdlClient)
        resetPdlClientMock(pdlClientCredentialClient)
    }

    @Test
    fun `skal feile hvis man ikke har med token`() {
        hentPersonKall(null)
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `skal kunne hente person fra pdl uten å ta med barn`() {
        val response = hentPerson(personident)

        verify(exactly = 1) { pdlClient.hentSøker(Fødselsnummer(personident)) }
        assertThat(response.visningsnavn).isEqualTo("fornavn etternavn")
        assertThat(response.barn).hasSize(0)
    }

    @Test
    fun `skal kunne hente person med barn fra pdl`() {
        val response = hentPersonMedBarn(personident)

        verify(exactly = 1) { pdlClient.hentSøker(Fødselsnummer(personident)) }
        assertThat(response.visningsnavn).isEqualTo("fornavn etternavn")
        assertThat(response.barn).hasSize(2)
    }
}
