package no.nav.tilleggsstonader.soknad.arkivering

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import no.nav.tilleggsstonader.kontrakter.dokarkiv.ArkiverDokumentResponse
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.soknad.infrastruktur.IntegrasjonerClient
import no.nav.tilleggsstonader.soknad.infrastruktur.database.JsonWrapper
import no.nav.tilleggsstonader.soknad.soknad.SøknadService
import no.nav.tilleggsstonader.soknad.soknad.domene.Skjema
import no.nav.tilleggsstonader.soknad.soknad.domene.VedleggRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class ArkiveringServiceTest {
    private val integrasjonerClient = mockk<IntegrasjonerClient>()
    private val søknadService = mockk<SøknadService>()
    private val vedleggRepository = mockk<VedleggRepository>()

    val arkiveringService =
        ArkiveringService(
            integrasjonerClient,
            søknadService,
            vedleggRepository,
        )

    private val skjema =
        Skjema(
            søknadJson = JsonWrapper(""),
            type = Stønadstype.BARNETILSYN,
            personIdent = "1",
            opprettetTid = LocalDateTime.now(),
            søknadPdf = byteArrayOf(12),
            søknadFrontendGitHash = "aabbccd",
        )

    val oppdaterSkjemaSlot = slot<Skjema>()

    @BeforeEach
    fun setUp() {
        oppdaterSkjemaSlot.clear()
        every { søknadService.hentSøknad(skjema.id) } returns skjema
        every { vedleggRepository.findBySøknadId(skjema.id) } returns emptyList()
        justRun { søknadService.oppdaterSøknad(capture(oppdaterSkjemaSlot)) }
    }

    @Test
    fun `skal oppdatere søknaden med journalpostId`() {
        val journalpostId = "journalpostId_1"
        every { integrasjonerClient.arkiver(any()) } returns
            ArkiverDokumentResponse(journalpostId, false, emptyList())
        arkiveringService.journalførSøknad(skjema.id, "callId")
        assertThat(oppdaterSkjemaSlot.captured.journalpostId).isEqualTo(journalpostId)
    }
}
