package no.nav.tilleggsstonader.soknad.soknad

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.tilleggsstonader.kontrakter.felles.ObjectMapperProvider.objectMapper
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.libs.utils.fnr.Fødselsnummer
import no.nav.tilleggsstonader.soknad.IntegrationTest
import no.nav.tilleggsstonader.soknad.person.PersonService
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.SøknadBarnetilsynDto
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.SøknadBarnetilsynUtil
import no.nav.tilleggsstonader.soknad.soknad.domene.SøknadRepository
import no.nav.tilleggsstonader.soknad.tokenSubject
import no.nav.tilleggsstonader.soknad.util.FileUtil
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.web.client.postForEntity
import java.time.LocalDate

class SøknadControllerTest : IntegrationTest() {

    @Autowired
    lateinit var personService: PersonService

    @Autowired
    lateinit var søknadRepository: SøknadRepository

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(søkerBearerToken(tokenSubject))
    }

    @Test
    fun `skal kunne sende inn en komplett søknad for barnetilsyn`() {
        val søknad = lagSøknadMedGyldigeBarn()
        val request = HttpEntity(søknad, headers)
        val response = restTemplate.postForEntity<Kvittering>(localhost("api/soknad/barnetilsyn"), request)
        assertThat(response.body!!.mottattTidspunkt.toLocalDate()).isEqualTo(LocalDate.now())

        verifiserLagretSøknad(Stønadstype.BARNETILSYN, "søknad/barnetilsyn.json")
    }

    private fun lagSøknadMedGyldigeBarn(): SøknadBarnetilsynDto {
        val barnIdenter = personService.hentSøker(Fødselsnummer(tokenSubject)).barn.map { it.ident }
        return SøknadBarnetilsynUtil.søknad.copy(
            barnMedBarnepass = listOf(
                SøknadBarnetilsynUtil.lagBarn(barnIdenter[0]),
                SøknadBarnetilsynUtil.lagBarn(barnIdenter[1]),
            ),
        )
    }

    @Test
    fun `skal feile når requesten mangler token`() {
        headers.remove(HttpHeaders.AUTHORIZATION)
        val request = HttpEntity<Any>(emptyMap<String, String>(), headers)
        assertThatThrownBy {
            restTemplate.postForEntity<Kvittering>(localhost("api/soknad/barnetilsyn"), request)
        }.hasMessage("""401 : "{"type":"about:blank","title":"Unauthorized","status":401,"detail":"Ukjent feil","instance":"/api/soknad/barnetilsyn"}"""")
    }

    private fun verifiserLagretSøknad(stønadstype: Stønadstype, filnavn: String) {
        val dbSøknad = søknadRepository.findAll().single()
        val søknadFraDb = objectMapper.readValue<Map<String, Any>>(dbSøknad.søknadJson.json).toMutableMap()
        søknadFraDb["mottattTidspunkt"] = "2023-09-25T21:32:18.22631"

        assertThat(dbSøknad.personIdent).isEqualTo(tokenSubject)
        assertThat(dbSøknad.type).isEqualTo(stønadstype)
        try {
            // FileUtil.skrivJsonTilFil(filnavn, søknadFraDb)
            assertThat(søknadFraDb).isEqualTo(objectMapper.readValue<Map<String, Any>>(FileUtil.readFile(filnavn)))
        } catch (e: Throwable) {
            LoggerFactory.getLogger("testlogger")
                .error("Actual=${objectMapper.writeValueAsString(søknadFraDb)}")
            throw e
        }
    }
}
