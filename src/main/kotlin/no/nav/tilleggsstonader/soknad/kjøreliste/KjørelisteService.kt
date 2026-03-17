package no.nav.tilleggsstonader.soknad.kjøreliste

import no.nav.tilleggsstonader.kontrakter.felles.JsonMapperProvider.jsonMapper
import no.nav.tilleggsstonader.kontrakter.felles.Skjematype
import no.nav.tilleggsstonader.kontrakter.søknad.InnsendtSkjema
import no.nav.tilleggsstonader.kontrakter.søknad.KjørelisteSkjema
import no.nav.tilleggsstonader.kontrakter.søknad.RammevedtakDto
import no.nav.tilleggsstonader.libs.sikkerhet.EksternBrukerUtils
import no.nav.tilleggsstonader.soknad.sak.DagligReisePrivatBilClient
import no.nav.tilleggsstonader.soknad.soknad.SkjemaService
import no.nav.tilleggsstonader.soknad.soknad.SøknadValideringException
import no.nav.tilleggsstonader.soknad.soknad.domene.SkjemaRepository
import org.springframework.stereotype.Service
import tools.jackson.module.kotlin.readValue
import java.time.LocalDateTime
import kotlin.random.Random

@Service
class KjørelisteService(
    private val skjemaService: SkjemaService,
    private val dagligReisePrivatBilClient: DagligReisePrivatBilClient,
    private val skjemaRepository: SkjemaRepository,
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
        validerKjøreliste(kjørelisteDto)

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

    fun validerKjøreliste(kjørelisteDto: KjørelisteDto) {
        val ident = EksternBrukerUtils.hentFnrFraToken()
        val tidligereInnsendeUkeIntervaller = hentTidligereInnsendeUkeIntervaller(ident, kjørelisteDto.reiseId)

        kjørelisteDto.reisedagerPerUkeAvsnitt.forEach { uke ->
            if (Uke(
                    uke.reisedager
                        .first()
                        .dato.verdi,
                ) in tidligereInnsendeUkeIntervaller
            ) {
                throw SøknadValideringException("${uke.ukeLabel} er allerede sendt inn. Kan ikke sende inn på nytt")
            }
        }
    }

    private fun hentTidligereInnsendeUkeIntervaller(
        ident: String,
        reiseId: String,
    ): Set<Uke> =
        skjemaRepository
            .findByPersonIdentAndType(ident, Skjematype.DAGLIG_REISE_KJØRELISTE)
            .map { jsonMapper.readValue<InnsendtSkjema<KjørelisteSkjema>>(it.skjemaJson.json) }
            .filter { it.skjema.reiseId == reiseId }
            .flatMap { it.skjema.reisedagerPerUkeAvsnitt }
            .map {
                Uke(
                    it.reisedager
                        .first()
                        .dato.verdi,
                )
            }.toSet()
}
