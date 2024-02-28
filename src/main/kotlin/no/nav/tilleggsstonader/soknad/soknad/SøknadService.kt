package no.nav.tilleggsstonader.soknad.soknad

import no.nav.familie.prosessering.internal.TaskService
import no.nav.tilleggsstonader.kontrakter.felles.ObjectMapperProvider
import no.nav.tilleggsstonader.kontrakter.felles.Språkkode
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.kontrakter.søknad.DokumentasjonFelt
import no.nav.tilleggsstonader.kontrakter.søknad.Søknadsskjema
import no.nav.tilleggsstonader.kontrakter.søknad.Vedleggstype
import no.nav.tilleggsstonader.libs.utils.fnr.Fødselsnummer
import no.nav.tilleggsstonader.soknad.dokument.FamilieVedleggClient
import no.nav.tilleggsstonader.soknad.infrastruktur.database.JsonWrapper
import no.nav.tilleggsstonader.soknad.infrastruktur.database.repository.findByIdOrThrow
import no.nav.tilleggsstonader.soknad.person.PersonService
import no.nav.tilleggsstonader.soknad.person.dto.Barn
import no.nav.tilleggsstonader.soknad.prosessering.LagPdfTask
import no.nav.tilleggsstonader.soknad.prosessering.SendNotifikasjonTask
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.BarnetilsynMapper
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.SøknadBarnetilsynDto
import no.nav.tilleggsstonader.soknad.soknad.domene.Søknad
import no.nav.tilleggsstonader.soknad.soknad.domene.SøknadRepository
import no.nav.tilleggsstonader.soknad.soknad.domene.Vedlegg
import no.nav.tilleggsstonader.soknad.soknad.domene.VedleggRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class SøknadService(
    private val søknadRepository: SøknadRepository,
    private val vedleggRepository: VedleggRepository,
    private val barnetilsynMapper: BarnetilsynMapper,
    private val taskService: TaskService,
    private val personService: PersonService,
    private val familieVedleggClient: FamilieVedleggClient,
) {

    fun hentSøknad(id: UUID): Søknad {
        return søknadRepository.findByIdOrThrow(id)
    }

    fun finnVedleggTitlerForSøknad(id: UUID) = vedleggRepository.finnTitlerForSøknadId(id)

    fun oppdaterSøknad(søknad: Søknad) {
        søknadRepository.update(søknad)
    }

    @Transactional
    fun lagreSøknad(
        personIdent: String,
        mottattTidspunkt: LocalDateTime,
        søknad: SøknadBarnetilsynDto,
    ): UUID {
        val barn = personService.hentSøker(Fødselsnummer(personIdent)).barn.associateBy { it.ident }
        verifiserHarGyldigeBarn(søknad, barn)

        val vedlegg = hentVedlegg(søknad.dokumentasjon)
        val type = Stønadstype.BARNETILSYN
        val språkkode = Språkkode.NB

        val opprettetSøknad = lagreSøknad(
            type = type,
            personIdent = personIdent,
            mottattTidspunkt = mottattTidspunkt,
            søknad = barnetilsynMapper.map(søknad, barn, språkkode),
            språkkode = språkkode,
            vedlegg = vedlegg,
        )
        taskService.save(LagPdfTask.opprettTask(opprettetSøknad))
        taskService.save(SendNotifikasjonTask.opprettTask(opprettetSøknad))
        return opprettetSøknad.id
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

    private fun hentVedlegg(id: UUID) = try {
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

    private fun <T> lagreSøknad(
        type: Stønadstype,
        personIdent: String,
        mottattTidspunkt: LocalDateTime,
        søknad: T,
        språkkode: Språkkode,
        vedlegg: List<Vedleggholder>,
    ): Søknad {
        val søknadsskjema = Søknadsskjema(personIdent, mottattTidspunkt, språkkode, søknad)
        val søknadDb = søknadRepository.insert(
            Søknad(
                type = type,
                personIdent = personIdent,
                søknadJson = JsonWrapper(ObjectMapperProvider.objectMapper.writeValueAsString(søknadsskjema)),
            ),
        )
        lagreVedlegg(søknadDb, vedlegg)
        return søknadDb
    }

    private fun lagreVedlegg(
        søknadDb: Søknad,
        vedlegg: List<Vedleggholder>,
    ) {
        vedleggRepository.insertAll(
            vedlegg.map {
                Vedlegg(
                    id = it.id,
                    søknadId = søknadDb.id,
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
