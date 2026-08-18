package no.nav.tilleggsstonader.soknad.kjøreliste

import io.mockk.every
import no.nav.tilleggsstonader.soknad.IntegrationTest
import no.nav.tilleggsstonader.soknad.infrastruktur.DagligReisePrivatBilClientConfig
import no.nav.tilleggsstonader.soknad.integrasjonstest.extensions.kall.hentKjørelister
import no.nav.tilleggsstonader.soknad.integrasjonstest.extensions.kall.hentKjørelisterKall
import no.nav.tilleggsstonader.soknad.sak.DagligReisePrivatBilClient
import no.nav.tilleggsstonader.soknad.tokenSubject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

class KjørelisteControllerTest : IntegrationTest() {
    @Autowired
    private lateinit var dagligReisePrivatBilClient: DagligReisePrivatBilClient

    @BeforeEach
    fun resetKjørelisteMock() {
        DagligReisePrivatBilClientConfig.resetMock(dagligReisePrivatBilClient)
    }

    @Test
    fun `skal returnere null når ingen kjøreliste finnes for reiseId`() {
        val result = hentKjørelister("ukjent-reise")

        assertThat(result).isNull()
    }

    @Test
    fun `skal returnere kjøreliste fra sak`() {
        val dato = LocalDate.of(2025, 1, 6)
        every { dagligReisePrivatBilClient.hentManueltRegistrertKjørelisteForReise("1") } returns
            ManueltRegistrertKjøreliste(
                listOf(ManueltRegistrertKjørelisteDag(dato = dato, harKjørt = true, parkeringsutgift = 50)),
            )

        val result = hentKjørelister("1")

        assertThat(result).isNotNull
        assertThat(result!!.reisedager).hasSize(1)
        with(result.reisedager[0]) {
            assertThat(this.dato).isEqualTo(dato)
            assertThat(harKjørt).isTrue()
            assertThat(parkeringsutgift).isEqualTo(50)
        }
    }

    @Test
    fun `skal returnere kjøreliste sendt inn via soknad-api`() {
        val kjøreliste = KjørelisteTestdata.kjørelisteDto()

        restTestClient
            .post()
            .uri("/api/kjorelister")
            .body(kjøreliste)
            .medSøkerBearerToken(tokenSubject)
            .exchange()
            .expectStatus()
            .isOk

        val result = hentKjørelister(kjøreliste.reiseId)

        assertThat(result).isNotNull
        assertThat(result!!.reisedager).hasSize(3)
        assertThat(result.reisedager.map { it.dato }).containsExactly(
            LocalDate.of(2025, 1, 6),
            LocalDate.of(2025, 1, 7),
            LocalDate.of(2025, 1, 10),
        )
        assertThat(result.reisedager).allMatch { it.harKjørt }
        assertThat(result.reisedager.map { it.parkeringsutgift }).containsExactly(130, 40, 90)
    }

    @Test
    fun `skal feile med 401 uten token`() {
        hentKjørelisterKall("1", personident = null)
            .expectStatus()
            .isUnauthorized
    }
}
