package no.nav.tilleggsstonader.soknad.arkivering

import no.nav.tilleggsstonader.libs.http.client.ProblemDetailException
import no.nav.tilleggsstonader.soknad.infrastruktur.IntegrasjonerClient
import no.nav.tilleggsstonader.soknad.soknad.Søknad
import no.nav.tilleggsstonader.soknad.soknad.SøknadService
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import java.util.UUID

@Service
class ArkiveringService(
    private val integrasjonerClient: IntegrasjonerClient,
    private val søknadService: SøknadService
) {

    // TODO legg til vedlegg
    fun journalførSøknad(søknadId: UUID, callId: String): String {
        val søknad = søknadService.hentSøknad(søknadId)
        val journalpostId: String = håndterJournalpostAlleredeOpprettet(søknad.personIdent, callId) {
            send(søknad)
        }

        val søknadMedJournalpostId = søknad.copy(journalpostId = journalpostId)
        søknadService.oppdaterSøknad(søknadMedJournalpostId)
        return journalpostId
    }

    private fun send(søknad: Søknad): String {
        val arkiverDokumentRequest = ArkiverDokumentRequestMapper.toDto(søknad, vedlegg)
        val dokumentResponse = integrasjonerClient.arkiver(arkiverDokumentRequest)
        return dokumentResponse.journalpostId
    }

    private fun håndterJournalpostAlleredeOpprettet(fnr: String, callId: String, journalfør: () -> String): String {
        return try {
            journalfør()
        } catch (e: ProblemDetailException) {
            if (e.cause is HttpClientErrorException.Conflict) {
                logger.warn("409 conflict for eksternReferanseId ved journalføring med callId: $callId.")
                hentJournalpostIdForBrukerOgEksternReferanseId(
                    callId,
                    fnr,
                )?.journalpostId
                    ?: error("Fant ikke journalpost for callId (eksternReferanseId) $callId")
            } else {
                throw e
            }
        }
    }
}