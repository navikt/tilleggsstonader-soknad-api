package no.nav.tilleggsstonader.soknad.soknad

import io.mockk.every
import io.mockk.mockk
import no.nav.tilleggsstonader.libs.test.fnr.FnrGenerator
import no.nav.tilleggsstonader.libs.utils.fnr.Fødselsnummer
import no.nav.tilleggsstonader.soknad.person.PersonService
import no.nav.tilleggsstonader.soknad.person.dto.PersonMedBarnDto
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.SøknadBarnetilsynUtil
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class SøknadServiceTest {

    private val personService = mockk<PersonService>()

    private val service = SøknadService(
        søknadRepository = mockk(),
        barnetilsynMapper = mockk(),
        taskService = mockk(),
        personService = personService,
    )

    @Test
    fun `skal kaste feil hvis man prøver å sende inn barn som ikke finnes på søreken`() {
        val personIdent = FnrGenerator.generer()
        val person = mockk<PersonMedBarnDto>()
        every { person.barn } returns listOf()
        every { personService.hentSøker(Fødselsnummer(personIdent)) } returns person

        val søknad = SøknadBarnetilsynUtil.søknad

        assertThatThrownBy {
            service.lagreSøknad(personIdent, LocalDateTime.now(), søknad)
        }.hasMessageContaining("Prøver å sende inn identer på barnen")
    }
}
