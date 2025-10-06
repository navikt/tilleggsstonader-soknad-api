package no.nav.tilleggsstonader.soknad.soknad

import no.nav.familie.prosessering.internal.TaskService
import no.nav.tilleggsstonader.kontrakter.felles.ObjectMapperProvider.objectMapper
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.kontrakter.søknad.DokumentasjonFelt
import no.nav.tilleggsstonader.kontrakter.søknad.InnsendtSkjema
import no.nav.tilleggsstonader.kontrakter.søknad.Skjemadata
import no.nav.tilleggsstonader.kontrakter.søknad.Vedleggstype
import no.nav.tilleggsstonader.libs.utils.fnr.Fødselsnummer
import no.nav.tilleggsstonader.soknad.dokument.FamilieVedleggClient
import no.nav.tilleggsstonader.soknad.infrastruktur.database.JsonWrapper
import no.nav.tilleggsstonader.soknad.infrastruktur.database.repository.findByIdOrThrow
import no.nav.tilleggsstonader.soknad.kjøreliste.KjørelisteDto
import no.nav.tilleggsstonader.soknad.kjøreliste.KjørelisteMapper
import no.nav.tilleggsstonader.soknad.person.PersonService
import no.nav.tilleggsstonader.soknad.person.dto.Barn
import no.nav.tilleggsstonader.soknad.prosessering.LagPdfTask
import no.nav.tilleggsstonader.soknad.prosessering.SendNotifikasjonTask
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.BarnetilsynMapper
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.SøknadBarnetilsynDto
import no.nav.tilleggsstonader.soknad.soknad.domene.Skjema
import no.nav.tilleggsstonader.soknad.soknad.domene.SkjemaRepository
import no.nav.tilleggsstonader.soknad.soknad.domene.Vedlegg
import no.nav.tilleggsstonader.soknad.soknad.domene.VedleggRepository
import no.nav.tilleggsstonader.soknad.soknad.læremidler.LæremidlerMapper
import no.nav.tilleggsstonader.soknad.soknad.læremidler.SøknadLæremidlerDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class SkjemaService(
    private val skjemaRepository: SkjemaRepository,
    private val vedleggRepository: VedleggRepository,
    private val barnetilsynMapper: BarnetilsynMapper,
    private val læremidlerMapper: LæremidlerMapper,
    private val taskService: TaskService,
    private val personService: PersonService,
    private val familieVedleggClient: FamilieVedleggClient,
) {
    fun hentSkjema(id: UUID): Skjema = skjemaRepository.findByIdOrThrow(id)

    fun oppdaterSkjema(skjema: Skjema) {
        skjemaRepository.update(skjema)
    }

    @Transactional
    fun lagreSøknadTilsynBarn(
        ident: String,
        mottattTidspunkt: LocalDateTime,
        søknad: SøknadBarnetilsynDto,
    ): UUID {
        val barn = personService.hentSøker(Fødselsnummer(ident), medBarn = true).barn.associateBy { it.ident }
        verifiserHarGyldigeBarn(søknad, barn)

        val vedlegg = hentVedlegg(søknad.dokumentasjon)
        val opprettetSkjema =
            lagreSkjema(
                type = Stønadstype.BARNETILSYN,
                innsendtSkjema = barnetilsynMapper.map(ident, mottattTidspunkt, barn, søknad),
                vedlegg = vedlegg,
                frontendGitHash = søknad.søknadMetadata.søknadFrontendGitHash,
            )
        taskService.save(LagPdfTask.opprettTask(opprettetSkjema))
        taskService.save(SendNotifikasjonTask.opprettTask(opprettetSkjema))
        return opprettetSkjema.id
    }

    @Transactional
    fun lagreLæremidlerSøknad(
        ident: String,
        mottattTidspunkt: LocalDateTime,
        søknad: SøknadLæremidlerDto,
    ): UUID {
        val vedlegg = hentVedlegg(søknad.dokumentasjon)

        val opprettetSkjema =
            lagreSkjema(
                type = Stønadstype.LÆREMIDLER,
                innsendtSkjema = læremidlerMapper.map(ident, mottattTidspunkt, søknad),
                vedlegg = vedlegg,
                frontendGitHash = søknad.søknadMetadata.søknadFrontendGitHash,
            )

        taskService.save(LagPdfTask.opprettTask(opprettetSkjema))
        taskService.save(SendNotifikasjonTask.opprettTask(opprettetSkjema))
        return opprettetSkjema.id
    }

    @Transactional
    fun lagreKjøreliste(
        ident: String,
        mottattTidspunkt: LocalDateTime,
        kjøreliste: KjørelisteDto,
    ): UUID {
        val vedlegg = hentVedlegg(kjøreliste.dokumentasjon)
        val stønadstype = finnStønadstypeForKjøreliste(kjøreliste)

        val opprettetSkjema =
            lagreSkjema(
                type = stønadstype,
                innsendtSkjema = KjørelisteMapper.map(ident, mottattTidspunkt, kjøreliste),
                vedlegg = vedlegg,
                frontendGitHash = kjøreliste.søknadMetadata.søknadFrontendGitHash,
            )

        taskService.save(LagPdfTask.opprettTask(opprettetSkjema))
        taskService.save(SendNotifikasjonTask.opprettTask(opprettetSkjema))
        return opprettetSkjema.id
    }

    private fun finnStønadstypeForKjøreliste(kjøreliste: KjørelisteDto): Stønadstype {
        // TODO - hente stønadstype for rammevedtak
        return Stønadstype.DAGLIG_REISE_TSO
    }

    private fun hentVedlegg(dokumentasjon: List<DokumentasjonFelt>): List<Vedleggholder> =
        dokumentasjon.flatMap {
            it.opplastedeVedlegg.map { vedlegg ->
                Vedleggholder(
                    id = vedlegg.id,
                    type = it.type,
                    navn = vedlegg.navn,
                    data = hentVedlegg(vedlegg.id),
                )
            }
        }

    private fun hentVedlegg(id: UUID) =
        try {
            familieVedleggClient.hentVedlegg(id)
        } catch (e: Exception) {
            throw RuntimeException("Feilet henting av vedlegg=$id", e)
        }

    private fun verifiserHarGyldigeBarn(
        søknad: SøknadBarnetilsynDto,
        barnIdenter: Map<String, Barn>,
    ) {
        val barnIdenterSøknad = søknad.barnMedBarnepass.map { it.ident }
        if (!barnIdenter.keys.containsAll(barnIdenterSøknad)) {
            error("Prøver å sende inn identer på barnen($barnIdenterSøknad) som ikke finnes på søkeren ($barnIdenter)")
        }
    }

    private fun <T : Skjemadata> lagreSkjema(
        type: Stønadstype,
        innsendtSkjema: InnsendtSkjema<T>,
        vedlegg: List<Vedleggholder>,
        frontendGitHash: String?,
    ): Skjema {
        val skjemaDb =
            skjemaRepository.insert(
                Skjema(
                    type = type,
                    personIdent = innsendtSkjema.ident,
                    skjemaJson = JsonWrapper(objectMapper.writeValueAsString(innsendtSkjema)),
                    frontendGitHash = frontendGitHash,
                ),
            )
        lagreVedlegg(skjemaDb, vedlegg)
        return skjemaDb
    }

    private fun lagreVedlegg(
        skjemaDb: Skjema,
        vedlegg: List<Vedleggholder>,
    ) {
        vedleggRepository.insertAll(
            vedlegg.map {
                Vedlegg(
                    id = it.id,
                    søknadId = skjemaDb.id,
                    type = it.type,
                    navn = it.navn,
                    innhold = it.data,
                )
            },
        )
    }

    private class Vedleggholder(
        val id: UUID,
        val type: Vedleggstype,
        val navn: String,
        val data: ByteArray,
    )
}
