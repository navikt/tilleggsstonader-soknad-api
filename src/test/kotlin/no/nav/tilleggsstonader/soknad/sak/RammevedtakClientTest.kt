package no.nav.tilleggsstonader.soknad.sak

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import no.nav.tilleggsstonader.kontrakter.felles.IdentStønadstype
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.web.client.RestTemplateBuilder
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
                .post("/api/ekstern/privat-bil/rammevedtak")
                .willReturn(
                    WireMock
                        .aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")
                        .withStatus(200),
                ),
        )

        client.hentRammevedtak(IdentStønadstype("ident", Stønadstype.DAGLIG_REISE_TSO))

        wireMockServer.verify(
            1,
            WireMock.postRequestedFor(WireMock.urlEqualTo("/api/ekstern/privat-bil/rammevedtak")),
        )
    }
}
