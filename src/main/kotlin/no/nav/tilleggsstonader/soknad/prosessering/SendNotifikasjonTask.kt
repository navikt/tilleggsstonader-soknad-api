package no.nav.tilleggsstonader.soknad.prosessering

import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.soknad.soknad.SøknadService
import no.nav.tilleggsstonader.soknad.soknad.domene.Søknad
import no.nav.tilleggsstonader.soknad.varsel.DittNavKafkaProducer
import org.springframework.stereotype.Service
import java.util.Properties
import java.util.UUID

@Service
@TaskStepBeskrivelse(taskStepType = SendNotifikasjonTask.TYPE, beskrivelse = "Send notifikasjon mottatt søknad")
class SendNotifikasjonTask(
    private val notifikasjonsService: DittNavKafkaProducer,
    private val søknadService: SøknadService,
) : AsyncTaskStep {
    override fun doTask(task: Task) {
        val søknad = søknadService.hentSøknad(UUID.fromString(task.payload))
        val message = lagNotifikasjonsMelding(søknad.type)
        val eventId = søknad.id.toString()

        notifikasjonsService.sendToKafka(søknad.personIdent, message, eventId)
    }

    private fun lagNotifikasjonsMelding(stønadstype: Stønadstype): String =
        when (stønadstype) {
            Stønadstype.BARNETILSYN -> "Vi har mottatt søknaden din om pass av barn."
            Stønadstype.LÆREMIDLER -> "Vi har mottatt søknaden din om læremidler."
            Stønadstype.BOUTGIFTER -> error("Har ikke laget søknad for $stønadstype")
        }

    companion object {
        const val TYPE = "SEND_NOTIFIKASJON"

        fun opprettTask(søknad: Søknad): Task {
            val properties =
                Properties().apply {
                    setProperty("søkersFødselsnummer", søknad.personIdent)
                    setProperty("type", søknad.type.name)
                }
            return Task(TYPE, søknad.id.toString(), properties)
        }
    }
}
