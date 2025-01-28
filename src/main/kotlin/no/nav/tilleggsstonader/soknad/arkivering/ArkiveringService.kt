package no.nav.tilleggsstonader.soknad.arkivering

import no.nav.tilleggsstonader.soknad.infrastruktur.IntegrasjonerClient
import no.nav.tilleggsstonader.soknad.soknad.SøknadService
import no.nav.tilleggsstonader.soknad.soknad.domene.Søknad
import no.nav.tilleggsstonader.soknad.soknad.domene.Vedlegg
import no.nav.tilleggsstonader.soknad.soknad.domene.VedleggRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ArkiveringService(
    private val integrasjonerClient: IntegrasjonerClient,
    private val søknadService: SøknadService,
    private val vedleggRepository: VedleggRepository,
) {
    fun journalførSøknad(
        søknadId: UUID,
        callId: String,
    ) {
        val søknad = søknadService.hentSøknad(søknadId)
        val vedlegg = vedleggRepository.findBySøknadId(søknadId)
        val journalpostId: String = send(søknad, vedlegg)

        søknadService.oppdaterSøknad(søknad.copy(journalpostId = journalpostId))
    }

    private fun send(
        søknad: Søknad,
        vedlegg: List<Vedlegg>,
    ): String {
        val arkiverDokumentRequest = ArkiverDokumentRequestMapper.toDto(søknad, vedlegg)
        val dokumentResponse = integrasjonerClient.arkiver(arkiverDokumentRequest)
        return dokumentResponse.journalpostId
    }
}
