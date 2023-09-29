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
import no.nav.tilleggsstonader.soknad.soknad.domene.Søknad
import no.nav.tilleggsstonader.soknad.soknad.domene.VedleggRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class ArkiveringServiceTest {

    private val integrasjonerClient = mockk<IntegrasjonerClient>()
    private val søknadService = mockk<SøknadService>()
    private val vedleggRepository = mockk<VedleggRepository>()

    val arkiveringService = ArkiveringService(
        integrasjonerClient,
        søknadService,
        vedleggRepository,
    )

    private val søknad = Søknad(
        søknadJson = JsonWrapper(""),
        type = Stønadstype.BARNETILSYN,
        personIdent = "1",
        opprettetTid = LocalDateTime.now(),
        søknadPdf = byteArrayOf(12),
    )

    val oppdaterSøknadSlot = slot<Søknad>()

    @BeforeEach
    fun setUp() {
        oppdaterSøknadSlot.clear()
        every { søknadService.hentSøknad(søknad.id) } returns søknad
        every { vedleggRepository.findBySøknadId(søknad.id) } returns emptyList()
        justRun { søknadService.oppdaterSøknad(capture(oppdaterSøknadSlot)) }
    }

    @Test
    fun `skal oppdatere søknaden med journalpostId`() {
        val journalpostId = "journalpostId_1"
        every { integrasjonerClient.arkiver(any()) } returns
            ArkiverDokumentResponse(journalpostId, false, emptyList())
        arkiveringService.journalførSøknad(søknad.id, "callId")
        assertThat(oppdaterSøknadSlot.captured.journalpostId).isEqualTo(journalpostId)
    }
}
