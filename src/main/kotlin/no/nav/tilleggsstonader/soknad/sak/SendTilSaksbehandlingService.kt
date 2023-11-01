package no.nav.tilleggsstonader.soknad.sak

import no.nav.tilleggsstonader.kontrakter.sak.journalføring.AutomatiskJournalføringRequest
import no.nav.tilleggsstonader.soknad.soknad.SøknadService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class SendTilSaksbehandlingService(
    private val sakbehandlingClient: SaksbehandlingClient,
    private val søknadService: SøknadService,
) {

    fun sendTilSak(søknadId: UUID) {
        val søknad = søknadService.hentSøknad(søknadId)
        val journalpostId = søknad.journalpostId ?: error("Søknad mangler journalpostId")
        sakbehandlingClient.sendTilSak(
            request = AutomatiskJournalføringRequest(
                personIdent = søknad.personIdent,
                journalpostId = journalpostId,
                stønadstype = søknad.type,
            ),
        )
    }
}
