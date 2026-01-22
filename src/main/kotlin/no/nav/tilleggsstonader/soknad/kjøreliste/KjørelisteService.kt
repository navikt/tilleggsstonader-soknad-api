package no.nav.tilleggsstonader.soknad.kjøreliste

import no.nav.tilleggsstonader.kontrakter.felles.IdentRequest
import no.nav.tilleggsstonader.libs.sikkerhet.EksternBrukerUtils
import no.nav.tilleggsstonader.soknad.sak.DagligReisePrivatBilClient
import no.nav.tilleggsstonader.soknad.soknad.SkjemaService
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlin.random.Random

@Service
class KjørelisteService(
    private val skjemaService: SkjemaService,
    private val dagligReisePrivatBilClient: DagligReisePrivatBilClient,
) {
    fun hentAlleRammevedtak(ident: IdentRequest): List<RammevedtakDto> = dagligReisePrivatBilClient.hentRammevedtak(ident)

    fun mottaKjøreliste(kjørelisteDto: KjørelisteDto): KjørelisteResponse {
        skjemaService.lagreKjøreliste(
            ident = EksternBrukerUtils.hentFnrFraToken(),
            mottattTidspunkt = LocalDateTime.now(),
            kjøreliste = kjørelisteDto,
        )

        // TODO - hente saksnummer fra rammevedtak
        val saksnummer = Random.nextInt(1000, 10000)
        return KjørelisteResponse(
            mottattTidspunkt = LocalDateTime.now(),
            saksnummer = saksnummer,
        )
    }
}
