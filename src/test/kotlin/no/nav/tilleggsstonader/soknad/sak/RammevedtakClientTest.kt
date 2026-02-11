package no.nav.tilleggsstonader.soknad.sak

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.restclient.RestTemplateBuilder
import java.net.URI

class RammevedtakClientTest {
    private lateinit var client: DagligReisePrivatBilClient
    private lateinit var wireMockServer: WireMockServer

    @BeforeEach
    fun setUp() {
        wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort())
        wireMockServer.start()
        val restOperations = RestTemplateBuilder().build()
        client = DagligReisePrivatBilClient(URI.create(wireMockServer.baseUrl()), restOperations)
    }

    @AfterEach
    fun tearDown() {
        wireMockServer.stop()
    }

    @Test
    fun `Skal kalle på riktig endepunkt når hentRammevedtak blir kjørt`() {
        wireMockServer.stubFor(
            WireMock
                .get("/api/ekstern/privat-bil/rammevedtak")
                .willReturn(
                    WireMock
                        .aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")
                        .withStatus(200),
                ),
        )

        client.hentRammevedtakForInnloggetBruker()

        wireMockServer.verify(
            1,
            WireMock.getRequestedFor(WireMock.urlEqualTo("/api/ekstern/privat-bil/rammevedtak")),
        )
    }
}
