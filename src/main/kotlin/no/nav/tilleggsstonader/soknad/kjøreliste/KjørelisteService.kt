package no.nav.tilleggsstonader.soknad.kjøreliste

import no.nav.tilleggsstonader.kontrakter.felles.JsonMapperProvider.jsonMapper
import no.nav.tilleggsstonader.kontrakter.felles.Skjematype
import no.nav.tilleggsstonader.kontrakter.søknad.InnsendtSkjema
import no.nav.tilleggsstonader.kontrakter.søknad.KjørelisteSkjema
import no.nav.tilleggsstonader.kontrakter.søknad.RammevedtakDto
import no.nav.tilleggsstonader.libs.sikkerhet.EksternBrukerUtils
import no.nav.tilleggsstonader.soknad.sak.DagligReisePrivatBilClient
import no.nav.tilleggsstonader.soknad.soknad.SkjemaService
import org.springframework.stereotype.Service
import tools.jackson.module.kotlin.readValue
import java.time.LocalDateTime
import kotlin.random.Random

@Service
class KjørelisteService(
    private val skjemaService: SkjemaService,
    private val dagligReisePrivatBilClient: DagligReisePrivatBilClient,
) {
    fun hentAlleRammevedtakForInnloggetBruker(): List<RammevedtakDto> = dagligReisePrivatBilClient.hentRammevedtakForInnloggetBruker()

    fun hentRammevedtakForInnloggetBruker(reiseId: String): RammevedtakDto {
        val rammevedtak =
            dagligReisePrivatBilClient
                .hentRammevedtakForInnloggetBruker()
                .first { it.reiseId == reiseId }
        return rammevedtak.copy(uker = rammevedtak.uker.filter { uke -> uke.kanSendeInnKjøreliste })
    }

    fun hentKjørelisterForReise(reiseId: String): KjørelisteDto? {
        val ident = EksternBrukerUtils.hentFnrFraToken()
        val skjemaer =
            skjemaService.hentSkjemaerForBruker(personIdent = ident, type = Skjematype.DAGLIG_REISE_KJØRELISTE)

        val kjørelister =
            skjemaer
                .map { jsonMapper.readValue<InnsendtSkjema<KjørelisteSkjema>>(it.skjemaJson.json).skjema }
                .filter { it.reiseId == reiseId }

        if (kjørelister.isEmpty()) return null

        val sammenslåtteKjørelister = slåSammenKjørelister(reiseId = reiseId, kjørelister = kjørelister)
        return sammenslåtteKjørelister.tilDto()
    }

    private fun slåSammenKjørelister(
        reiseId: String,
        kjørelister: List<KjørelisteSkjema>,
    ): KjørelisteSkjema =
        KjørelisteSkjema(
            reiseId = reiseId,
            reisedagerPerUkeAvsnitt = kjørelister.flatMap { it.reisedagerPerUkeAvsnitt },
            dokumentasjon = kjørelister.flatMap { it.dokumentasjon },
        )

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
