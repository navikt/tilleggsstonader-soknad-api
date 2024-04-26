package no.nav.tilleggsstonader.soknad.person

import io.mockk.verify
import no.nav.tilleggsstonader.libs.test.fnr.FnrGenerator
import no.nav.tilleggsstonader.libs.utils.fnr.Fødselsnummer
import no.nav.tilleggsstonader.soknad.IntegrationTest
import no.nav.tilleggsstonader.soknad.infrastruktur.PdlClientConfig.Companion.resetPdlClientMock
import no.nav.tilleggsstonader.soknad.person.pdl.PdlClientCredentialClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

class PersonServiceIntegrationTest : IntegrationTest() {

    @Autowired
    lateinit var personService: PersonService

    @Autowired
    lateinit var pdlClientCredentialClient: PdlClientCredentialClient

    @AfterEach
    override fun tearDown() {
        resetPdlClientMock(pdlClientCredentialClient)
    }

    @Test
    fun `skal bruke cache ved oppslag av person`() {
        val fnr1 = FnrGenerator.generer(LocalDate.now())
        val fnr2 = FnrGenerator.generer(LocalDate.now().minusYears(1))
        personService.hentSøker(Fødselsnummer(fnr1))
        personService.hentSøker(Fødselsnummer(fnr1))

        personService.hentSøker(Fødselsnummer(fnr2))

        verify(exactly = 1) { pdlClient.hentSøker(eq(Fødselsnummer(fnr1))) }
        verify(exactly = 1) { pdlClient.hentSøker(eq(Fødselsnummer(fnr2))) }
    }
}
