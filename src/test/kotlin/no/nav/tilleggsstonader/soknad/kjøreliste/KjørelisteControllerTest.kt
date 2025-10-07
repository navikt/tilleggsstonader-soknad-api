package no.nav.tilleggsstonader.soknad.kjøreliste

import io.mockk.verify
import no.nav.tilleggsstonader.kontrakter.dokarkiv.ArkiverDokumentRequest
import no.nav.tilleggsstonader.soknad.IntegrationTest
import no.nav.tilleggsstonader.soknad.infrastruktur.IntegrasjonerClient
import no.nav.tilleggsstonader.soknad.integrasjonstest.extensions.tasks.kjørTasksKlareForProsesseringTilIngenTasksIgjen
import no.nav.tilleggsstonader.soknad.soknad.Kvittering
import no.nav.tilleggsstonader.soknad.soknad.domene.SkjemaRepository
import no.nav.tilleggsstonader.soknad.tokenSubject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.web.client.postForEntity
import java.time.LocalDate

class KjørelisteControllerTest : IntegrationTest() {
    @Autowired
    lateinit var skjemaRepository: SkjemaRepository

    @Autowired
    lateinit var integrasjonerClient: IntegrasjonerClient

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(søkerBearerToken(tokenSubject))
    }

    @Test
    fun `skal kunne sende inn en komplett kjøreliste`() {
        val request = HttpEntity(KjørelisteTestdata.kjørelisteDto(), headers)
        val response = restTemplate.postForEntity<Kvittering>(localhost("api/kjorelister"), request)
        assertThat(response.body!!.mottattTidspunkt.toLocalDate()).isEqualTo(LocalDate.now())

        kjørTasksKlareForProsesseringTilIngenTasksIgjen()

        val søknad =
            with(skjemaRepository.findAll()) {
                assertThat(this).hasSize(1)
                this.single()
            }

        val matcher: (req: ArkiverDokumentRequest) -> Boolean = { req ->
            req.eksternReferanseId == søknad.id.toString()
        }

        // Verifiserer at søknad har blitt arkivert
        verify { integrasjonerClient.arkiver(match(matcher)) }
    }
}
