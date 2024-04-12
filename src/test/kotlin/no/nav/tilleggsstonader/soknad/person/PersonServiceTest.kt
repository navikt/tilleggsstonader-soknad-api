package no.nav.tilleggsstonader.soknad.person

import io.mockk.every
import io.mockk.mockk
import no.nav.tilleggsstonader.libs.test.fnr.FnrGenerator
import no.nav.tilleggsstonader.libs.utils.fnr.Fødselsnummer
import no.nav.tilleggsstonader.soknad.infrastruktur.exception.GradertBrukerException
import no.nav.tilleggsstonader.soknad.infrastruktur.lagPdlBarn
import no.nav.tilleggsstonader.soknad.infrastruktur.lagPdlSøker
import no.nav.tilleggsstonader.soknad.person.pdl.PdlClient
import no.nav.tilleggsstonader.soknad.person.pdl.PdlClientCredentialClient
import no.nav.tilleggsstonader.soknad.person.pdl.dto.AdressebeskyttelseGradering
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

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

    @Test
    fun `søker er gradert FORTROLIG - skal kaste feil`() {
        every { pdlClient.hentSøker(any()) } returns
            lagPdlSøker(adressebeskyttelse = AdressebeskyttelseGradering.FORTROLIG)

        assertDoesNotThrow { service.hentSøker(identSøker) }
    }

    @Test
    fun `søker er gradert STRENGT_FORTROLIG - skal kaste feil`() {
        every { pdlClient.hentSøker(any()) } returns
            lagPdlSøker(adressebeskyttelse = AdressebeskyttelseGradering.STRENGT_FORTROLIG)

        assertThatThrownBy {
            service.hentSøker(identSøker)
        }.isInstanceOf(GradertBrukerException::class.java)
    }

    @Test
    fun `barn er gradert STRENGT_FORTROLIG_UTLAND - skal kaste feil`() {
        every { pdlClient.hentSøker(any()) } returns lagPdlSøker()
        val barn = lagPdlBarn(
            FnrGenerator.generer(),
            adressebeskyttelse = AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND,
        )
        every { pdlClientCredentialClient.hentBarn(any()) } returns listOf(barn).toMap()

        assertThatThrownBy {
            service.hentSøker(identSøker)
        }.isInstanceOf(GradertBrukerException::class.java)
    }
}
