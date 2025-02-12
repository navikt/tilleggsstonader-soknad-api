package no.nav.tilleggsstonader.soknad.soknad

import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.every
import no.nav.tilleggsstonader.kontrakter.felles.ObjectMapperProvider.objectMapper
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.libs.test.fnr.FnrGenerator
import no.nav.tilleggsstonader.libs.utils.osloDateNow
import no.nav.tilleggsstonader.soknad.IntegrationTest
import no.nav.tilleggsstonader.soknad.infrastruktur.PdlClientConfig.Companion.resetPdlClientMock
import no.nav.tilleggsstonader.soknad.infrastruktur.lagPdlBarn
import no.nav.tilleggsstonader.soknad.person.PersonService
import no.nav.tilleggsstonader.soknad.person.pdl.PdlClientCredentialClient
import no.nav.tilleggsstonader.soknad.person.pdl.dto.AdressebeskyttelseGradering
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.SøknadBarnetilsynUtil
import no.nav.tilleggsstonader.soknad.soknad.domene.SøknadRepository
import no.nav.tilleggsstonader.soknad.soknad.læremidler.SøknadLæremidlerUtil
import no.nav.tilleggsstonader.soknad.tokenSubject
import no.nav.tilleggsstonader.soknad.util.FileUtil
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
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

    @Autowired
    lateinit var pdlClientCredentialClient: PdlClientCredentialClient

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(søkerBearerToken(tokenSubject))
    }

    @AfterEach
    override fun tearDown() {
        resetPdlClientMock(pdlClientCredentialClient)
        super.tearDown()
    }

    @Test
    fun `skal kunne sende inn en komplett søknad for barnetilsyn`() {
        val request = HttpEntity(SøknadBarnetilsynUtil.søknad, headers)
        val response = restTemplate.postForEntity<Kvittering>(localhost("api/soknad/pass-av-barn"), request)
        assertThat(response.body!!.mottattTidspunkt.toLocalDate()).isEqualTo(osloDateNow())

        verifiserLagretSøknad(Stønadstype.BARNETILSYN, "søknad/barnetilsyn/barnetilsyn.json")
    }

    @Test
    fun `skal kunne sende inn en komplett søknad for læremidler`() {
        val request = HttpEntity(SøknadLæremidlerUtil.søknad, headers)
        val response = restTemplate.postForEntity<Kvittering>(localhost("api/soknad/laremidler"), request)
        assertThat(response.body!!.mottattTidspunkt.toLocalDate()).isEqualTo(osloDateNow())

        verifiserLagretSøknad(Stønadstype.LÆREMIDLER, "søknad/læremidler/læremidler.json")
    }

    @Test
    fun `skal feile når requesten mangler token`() {
        headers.remove(HttpHeaders.AUTHORIZATION)
        val request = HttpEntity<Any>(emptyMap<String, String>(), headers)
        assertThatThrownBy {
            restTemplate.postForEntity<Kvittering>(localhost("api/soknad/pass-av-barn"), request)
        }.hasMessage(
            """401 : "{"type":"about:blank","title":"Unauthorized","status":401,"detail":"Ukjent feil","instance":"/api/soknad/pass-av-barn"}"""",
        )
    }

    @Test
    fun `skal feile hvis man prøver å sende inn søknad hvis barn har høyere gradering enn søker`() {
        val request = HttpEntity(SøknadBarnetilsynUtil.søknad, headers)

        val identBarn = FnrGenerator.generer(LocalDate.now().minusYears(3))
        val barn = lagPdlBarn(identBarn, adressebeskyttelse = AdressebeskyttelseGradering.FORTROLIG)
        every { pdlClientCredentialClient.hentBarn(any()) } returns mapOf(barn)

        assertThatThrownBy {
            restTemplate.postForEntity<Kvittering>(localhost("api/soknad/pass-av-barn"), request)
        }.hasMessage(
            """400 : "{"type":"about:blank","title":"Bad Request","status":400,"detail":"ROUTING_GAMMEL_SØKNAD","instance":"/api/soknad/pass-av-barn"}"""",
        )
    }

    private fun verifiserLagretSøknad(
        stønadstype: Stønadstype,
        filnavn: String,
    ) {
        val dbSøknad = søknadRepository.findAll().single()
        val søknadFraDb = objectMapper.readValue<Map<String, Any>>(dbSøknad.søknadJson.json).toMutableMap()
        søknadFraDb["mottattTidspunkt"] = "2023-09-25T21:32:18.22631"

        assertThat(dbSøknad.personIdent).isEqualTo(tokenSubject)
        assertThat(dbSøknad.type).isEqualTo(stønadstype)
        try {
            FileUtil.skrivJsonTilFil(filnavn, søknadFraDb)
            assertThat(søknadFraDb).isEqualTo(objectMapper.readValue<Map<String, Any>>(FileUtil.readFile(filnavn)))
        } catch (e: Throwable) {
            LoggerFactory
                .getLogger("testlogger")
                .error("Actual=${objectMapper.writeValueAsString(søknadFraDb)}")
            throw e
        }
    }
}
