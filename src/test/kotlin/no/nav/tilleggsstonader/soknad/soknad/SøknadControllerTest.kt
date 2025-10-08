package no.nav.tilleggsstonader.soknad.soknad

import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.every
import no.nav.tilleggsstonader.kontrakter.felles.ObjectMapperProvider.objectMapper
import no.nav.tilleggsstonader.kontrakter.felles.Skjematype
import no.nav.tilleggsstonader.libs.test.fnr.FnrGenerator
import no.nav.tilleggsstonader.soknad.IntegrationTest
import no.nav.tilleggsstonader.soknad.infrastruktur.PdlClientConfig.Companion.resetPdlClientMock
import no.nav.tilleggsstonader.soknad.infrastruktur.lagPdlBarn
import no.nav.tilleggsstonader.soknad.integrasjonstest.extensions.kall.sendInnSøknadBarnetilsyn
import no.nav.tilleggsstonader.soknad.integrasjonstest.extensions.kall.sendInnSøknadBarnetilsynKall
import no.nav.tilleggsstonader.soknad.integrasjonstest.extensions.kall.sendInnSøknadLæremidler
import no.nav.tilleggsstonader.soknad.integrasjonstest.extensions.kall.sendInnSøknadLæremidlerKall
import no.nav.tilleggsstonader.soknad.person.pdl.PdlClientCredentialClient
import no.nav.tilleggsstonader.soknad.person.pdl.dto.AdressebeskyttelseGradering
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.SøknadBarnetilsynUtil
import no.nav.tilleggsstonader.soknad.soknad.domene.SkjemaRepository
import no.nav.tilleggsstonader.soknad.soknad.læremidler.SøknadLæremidlerUtil
import no.nav.tilleggsstonader.soknad.tokenSubject
import no.nav.tilleggsstonader.soknad.util.FileUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

class SøknadControllerTest : IntegrationTest() {
    @Autowired
    lateinit var skjemaRepository: SkjemaRepository

    @Autowired
    lateinit var pdlClientCredentialClient: PdlClientCredentialClient

    @AfterEach
    override fun tearDown() {
        resetPdlClientMock(pdlClientCredentialClient)
        super.tearDown()
    }

    @Test
    fun `skal kunne sende inn en komplett søknad for barnetilsyn`() {
        val response = sendInnSøknadBarnetilsyn(SøknadBarnetilsynUtil.søknadBarnetilsyn)
        assertThat(response.mottattTidspunkt.toLocalDate()).isEqualTo(LocalDate.now())

        verifiserLagretSøknad(Skjematype.SØKNAD_BARNETILSYN, "søknad/barnetilsyn/barnetilsyn.json")
    }

    @Test
    fun `skal kunne sende inn en komplett søknad for læremidler`() {
        val response = sendInnSøknadLæremidler(SøknadLæremidlerUtil.søknadLæremidler)
        assertThat(response.mottattTidspunkt.toLocalDate()).isEqualTo(LocalDate.now())

        verifiserLagretSøknad(Skjematype.SØKNAD_LÆREMIDLER, "søknad/læremidler/læremidler.json")
    }

    @Test
    fun `skal feile når requesten mangler token`() {
        sendInnSøknadLæremidlerKall(SøknadLæremidlerUtil.søknadLæremidler, medToken = false)
            .expectStatus()
            .isUnauthorized
            .expectBody()
    }

    @Test
    fun `skal feile hvis man prøver å sende inn søknad hvis barn har høyere gradering enn søker`() {
        val identBarn = FnrGenerator.generer(LocalDate.now().minusYears(3))
        val barn = lagPdlBarn(identBarn, adressebeskyttelse = AdressebeskyttelseGradering.FORTROLIG)
        every { pdlClientCredentialClient.hentBarn(any()) } returns mapOf(barn)

        sendInnSøknadBarnetilsynKall(SøknadBarnetilsynUtil.søknadBarnetilsyn)
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.detail")
            .isEqualTo("ROUTING_GAMMEL_SØKNAD")
    }

    private fun verifiserLagretSøknad(
        skjematype: Skjematype,
        filnavn: String,
    ) {
        val dbSøknad = skjemaRepository.findAll().single()
        val søknadFraDb = objectMapper.readValue<Map<String, Any>>(dbSøknad.skjemaJson.json).toMutableMap()
        søknadFraDb["mottattTidspunkt"] = "2023-09-25T21:32:18.22631"

        assertThat(dbSøknad.personIdent).isEqualTo(tokenSubject)
        assertThat(dbSøknad.type).isEqualTo(skjematype)
        assertThat(dbSøknad.frontendGitHash).isEqualTo("aabbccd")
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
