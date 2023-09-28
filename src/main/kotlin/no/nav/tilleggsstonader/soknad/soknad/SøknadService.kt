package no.nav.tilleggsstonader.soknad.soknad

import no.nav.familie.prosessering.internal.TaskService
import no.nav.tilleggsstonader.kontrakter.felles.ObjectMapperProvider
import no.nav.tilleggsstonader.kontrakter.felles.Språkkode
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.kontrakter.søknad.Søknadsskjema
import no.nav.tilleggsstonader.soknad.infrastruktur.database.JsonWrapper
import no.nav.tilleggsstonader.soknad.infrastruktur.database.repository.findByIdOrThrow
import no.nav.tilleggsstonader.soknad.prosessering.LagPdfTask
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.BarnetilsynMapper
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.SøknadBarnetilsynDto
import no.nav.tilleggsstonader.soknad.soknad.domene.Søknad
import no.nav.tilleggsstonader.soknad.soknad.domene.SøknadRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class SøknadService(
    private val søknadRepository: SøknadRepository,
    private val barnetilsynMapper: BarnetilsynMapper,
    private val taskService: TaskService,
) {

    fun hentSøknad(id: UUID): Søknad {
        return søknadRepository.findByIdOrThrow(id)
    }

    fun oppdaterSøknad(søknad: Søknad) {
        søknadRepository.update(søknad)
    }

    @Transactional
    fun lagreSøknad(
        personIdent: String,
        mottattTidspunkt: LocalDateTime,
        søknad: SøknadBarnetilsynDto,
    ) {
        val type = Stønadstype.BARNETILSYN
        val opprettetSøknad = lagreSøknad(
            type = type,
            personIdent = personIdent,
            mottattTidspunkt = mottattTidspunkt,
            søknad = barnetilsynMapper.map(søknad),
            språkkode = Språkkode.NB,
        )
        taskService.save(LagPdfTask.opprettTask(opprettetSøknad))
    }

    private fun <T> lagreSøknad(
        type: Stønadstype,
        personIdent: String,
        mottattTidspunkt: LocalDateTime,
        søknad: T,
        språkkode: Språkkode,
    ): Søknad {
        val søknadsskjema = Søknadsskjema(personIdent, mottattTidspunkt, språkkode, søknad)
        val søknadDb = søknadRepository.insert(
            Søknad(
                type = type,
                personIdent = personIdent,
                søknadJson = JsonWrapper(ObjectMapperProvider.objectMapper.writeValueAsString(søknadsskjema)),
            ),
        )
        // TODO lagre vedlegg
        return søknadDb
    }
}
