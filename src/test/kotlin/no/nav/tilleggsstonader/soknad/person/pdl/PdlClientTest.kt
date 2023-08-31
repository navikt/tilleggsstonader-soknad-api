package no.nav.tilleggsstonader.soknad.person.pdl

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import no.nav.tilleggsstonader.libs.test.fnr.FnrGenerator
import no.nav.tilleggsstonader.libs.utils.fnr.Fødselsnummer
import no.nav.tilleggsstonader.soknad.person.pdl.dto.AdressebeskyttelseGradering
import no.nav.tilleggsstonader.soknad.util.FileUtil.readFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.web.client.RestOperations
import java.net.URI

class PdlClientTest {

    @AfterEach
    fun afterEach() {
        wiremockServerItem.resetAll()
    }

    @Test
    fun `skal kunne håndtere response for søker-query mot pdl`() {
        wiremockServerItem.stubFor(
            WireMock.post(WireMock.anyUrl())
                .willReturn(WireMock.okJson(readFile("pdl/søkerResponse.json"))),
        )

        val response = pdlClient.hentSøker(Fødselsnummer(FnrGenerator.generer()))

        assertThat(response.navn.single().visningsnavn()).isEqualTo("Julius aka Julenissen")
        assertThat(response.adressebeskyttelse.single().gradering).isEqualTo(AdressebeskyttelseGradering.UGRADERT)
        assertThat(response.bostedsadresse).hasSize(1)
        assertThat(response.forelderBarnRelasjon).hasSize(1)
    }

    companion object {
        private val restOperations: RestOperations = RestTemplateBuilder().build()
        lateinit var pdlClient: PdlClient
        lateinit var wiremockServerItem: WireMockServer

        @BeforeAll
        @JvmStatic
        fun initClass() {
            wiremockServerItem = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort())
            wiremockServerItem.start()
            pdlClient = PdlClient(URI.create(wiremockServerItem.baseUrl()), restOperations)
        }

        @AfterAll
        @JvmStatic
        fun afterAll() {
            wiremockServerItem.stop()
        }
    }
}
