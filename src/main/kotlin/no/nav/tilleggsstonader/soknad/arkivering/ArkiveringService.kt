package no.nav.tilleggsstonader.soknad.arkivering

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.tilleggsstonader.kontrakter.dokarkiv.ArkiverDokumentResponse
import no.nav.tilleggsstonader.kontrakter.felles.ObjectMapperProvider.objectMapper
import no.nav.tilleggsstonader.soknad.infrastruktur.IntegrasjonerClient
import no.nav.tilleggsstonader.soknad.soknad.SøknadService
import no.nav.tilleggsstonader.soknad.soknad.domene.Søknad
import no.nav.tilleggsstonader.soknad.soknad.domene.Vedlegg
import no.nav.tilleggsstonader.soknad.soknad.domene.VedleggRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import java.util.UUID

@Service
class ArkiveringService(
    private val integrasjonerClient: IntegrasjonerClient,
    private val søknadService: SøknadService,
    private val vedleggRepository: VedleggRepository,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun journalførSøknad(søknadId: UUID, callId: String) {
        val søknad = søknadService.hentSøknad(søknadId)
        val vedlegg = vedleggRepository.findBySøknadId(søknadId)
        val journalpostId: String = håndterJournalpostAlleredeOpprettet(søknad.personIdent, callId) {
            send(søknad, vedlegg)
        }

        søknadService.oppdaterSøknad(søknad.copy(journalpostId = journalpostId))
    }

    private fun send(søknad: Søknad, vedlegg: List<Vedlegg>): String {
        val arkiverDokumentRequest = ArkiverDokumentRequestMapper.toDto(søknad, vedlegg)
        val dokumentResponse = integrasjonerClient.arkiver(arkiverDokumentRequest)
        return dokumentResponse.journalpostId
    }

    private fun håndterJournalpostAlleredeOpprettet(fnr: String, callId: String, journalfør: () -> String): String {
        return try {
            journalfør()
        } catch (e: HttpClientErrorException.Conflict) {
            if (e.cause is HttpClientErrorException.Conflict) {
                logger.warn("409 conflict for eksternReferanseId ved journalføring med callId: $callId.")
                objectMapper.readValue<ArkiverDokumentResponse>(e.responseBodyAsString).journalpostId
            } else {
                throw e
            }
        }
    }
}
