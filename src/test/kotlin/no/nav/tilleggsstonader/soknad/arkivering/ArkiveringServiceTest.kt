package no.nav.tilleggsstonader.soknad.arkivering

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import no.nav.tilleggsstonader.kontrakter.dokarkiv.ArkiverDokumentResponse
import no.nav.tilleggsstonader.kontrakter.felles.Skjematype
import no.nav.tilleggsstonader.soknad.infrastruktur.IntegrasjonerClient
import no.nav.tilleggsstonader.soknad.infrastruktur.database.ByteArrayWrapper
import no.nav.tilleggsstonader.soknad.infrastruktur.database.JsonWrapper
import no.nav.tilleggsstonader.soknad.soknad.SkjemaService
import no.nav.tilleggsstonader.soknad.soknad.domene.Skjema
import no.nav.tilleggsstonader.soknad.soknad.domene.VedleggRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class ArkiveringServiceTest {
    private val integrasjonerClient = mockk<IntegrasjonerClient>()
    private val skjemaService = mockk<SkjemaService>()
    private val vedleggRepository = mockk<VedleggRepository>()

    val arkiveringService =
        ArkiveringService(
            integrasjonerClient,
            skjemaService,
            vedleggRepository,
        )

    private val skjema =
        Skjema(
            skjemaJson = JsonWrapper(""),
            type = Skjematype.SØKNAD_BARNETILSYN,
            personIdent = "1",
            opprettetTid = LocalDateTime.now(),
            skjemaPdf = ByteArrayWrapper(byteArrayOf(12)),
            frontendGitHash = "aabbccd",
        )

    val oppdaterSkjemaSlot = slot<Skjema>()

    @BeforeEach
    fun setUp() {
        oppdaterSkjemaSlot.clear()
        every { skjemaService.hentSkjema(skjema.id) } returns skjema
        every { vedleggRepository.findBySkjemaId(skjema.id) } returns emptyList()
        justRun { skjemaService.oppdaterSkjema(capture(oppdaterSkjemaSlot)) }
    }

    @Test
    fun `skal oppdatere søknaden med journalpostId`() {
        val journalpostId = "journalpostId_1"
        every { integrasjonerClient.arkiver(any()) } returns
            ArkiverDokumentResponse(journalpostId, false, emptyList())
        arkiveringService.journalførSkjema(skjema.id, "callId")
        assertThat(oppdaterSkjemaSlot.captured.journalpostId).isEqualTo(journalpostId)
    }
}
