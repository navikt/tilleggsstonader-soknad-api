package no.nav.tilleggsstonader.soknad.sak

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.client.RestClient
import java.net.URI

class RammevedtakClientTest {
    private lateinit var client: DagligReisePrivatBilClient
    private lateinit var wireMockServer: WireMockServer

    @BeforeEach
    fun setUp() {
        wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort())
        wireMockServer.start()
        val restClient = RestClient.builder().baseUrl(wireMockServer.baseUrl()).build()
        client = DagligReisePrivatBilClient(URI.create(wireMockServer.baseUrl()), restClient)
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
