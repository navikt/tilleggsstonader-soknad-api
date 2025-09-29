package no.nav.tilleggsstonader.soknad.sak

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import no.nav.tilleggsstonader.kontrakter.felles.IdentStønadstype
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.web.client.RestTemplate
import java.net.URI

class SaksbehandlingClientTest {
    @Test
    fun `Skal kalle på riktig endepunkt når harBehandlingUnderArbeid blir kjørt`() {
        wireMockServer.stubFor(
            WireMock
                .post("/api/ekstern/har-behandling")
                .willReturn(
                    WireMock
                        .aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("true")
                        .withStatus(200),
                ),
        )

        client.harBehandlingUnderArbeid(IdentStønadstype("ident", Stønadstype.BARNETILSYN))

        wireMockServer.verify(
            1,
            WireMock.postRequestedFor(WireMock.urlEqualTo("/api/ekstern/har-behandling")),
        )
    }

    companion object {
        private val restOperations: RestTemplate = RestTemplateBuilder().build()
        lateinit var client: SaksbehandlingClient
        lateinit var wireMockServer: WireMockServer

        @BeforeAll
        @JvmStatic
        fun initClass() {
            wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort())
            wireMockServer.start()
            client = SaksbehandlingClient(URI.create(wireMockServer.baseUrl()), restOperations)
        }

        @AfterAll
        @JvmStatic
        fun afterAll() {
            wireMockServer.stop()
        }
    }
}
