package no.nav.tilleggsstonader.soknad.arkivering

import no.nav.tilleggsstonader.kontrakter.felles.Skjematype
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.soknad.infrastruktur.IntegrasjonerClient
import no.nav.tilleggsstonader.soknad.soknad.SkjemaService
import no.nav.tilleggsstonader.soknad.soknad.domene.Skjema
import no.nav.tilleggsstonader.soknad.soknad.domene.Vedlegg
import no.nav.tilleggsstonader.soknad.soknad.domene.VedleggRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ArkiveringService(
    private val integrasjonerClient: IntegrasjonerClient,
    private val skjemaService: SkjemaService,
    private val vedleggRepository: VedleggRepository,
) {
    fun journalførSkjema(
        skjemaId: UUID,
        callId: String,
    ) {
        val skjema = skjemaService.hentSkjema(skjemaId)
        val vedlegg = vedleggRepository.findBySkjemaId(skjemaId)
        val journalpostId: String = send(skjema, vedlegg)

        skjemaService.oppdaterSkjema(skjema.copy(journalpostId = journalpostId))
    }

    private fun send(
        skjema: Skjema,
        vedlegg: List<Vedlegg>,
    ): String {
        val arkiverDokumentRequest = ArkiverDokumentRequestMapper.toDto(skjema, finnTilhørendeStønadstypeForSkjema(skjema), vedlegg)
        val dokumentResponse = integrasjonerClient.arkiver(arkiverDokumentRequest)
        return dokumentResponse.journalpostId
    }

    private fun finnTilhørendeStønadstypeForSkjema(skjema: Skjema): Stønadstype =
        when (skjema.type) {
            Skjematype.SØKNAD_BARNETILSYN -> Stønadstype.BARNETILSYN
            Skjematype.SØKNAD_LÆREMIDLER -> Stønadstype.LÆREMIDLER
            Skjematype.DAGLIG_REISE_KJØRELISTE -> hentStønadstypeFraTilhørendeRammevedtak(skjema)
            else -> error("Håndterer ikke skjema ${skjema.type}")
        }

    private fun hentStønadstypeFraTilhørendeRammevedtak(skjema: Skjema): Stønadstype {
        // TODO - implementer
        return Stønadstype.DAGLIG_REISE_TSO
    }
}
