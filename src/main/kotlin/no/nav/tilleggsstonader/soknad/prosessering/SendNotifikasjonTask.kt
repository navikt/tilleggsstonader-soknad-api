package no.nav.tilleggsstonader.soknad.prosessering

import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.tilleggsstonader.soknad.soknad.domene.Søknad
import no.nav.tilleggsstonader.soknad.varsel.DittNavKafkaProducer
import org.springframework.stereotype.Service
import java.util.*

@Service
@TaskStepBeskrivelse(taskStepType = SendNotifikasjonTask.TYPE, beskrivelse = "Send notifikasjon mottatt søknad")
class SendNotifikasjonTask(
    private val notifikasjonsService: DittNavKafkaProducer,
) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val personident = task.metadata.getProperty("personident")
        val message = lagNotifikasjonsMedling(task.type)
        val eventId = task.id.toString()

        notifikasjonsService.sendToKafka(personident, message, eventId)
    }

    private fun lagNotifikasjonsMedling(type: String): String {
        return "Vi har mottatt søknaden din om pass av barn."
    }

    companion object {
        const val TYPE = "SEND_NOTIFIKASJON"

        fun opprettTask(søknad: Søknad): Task {
            val properties = Properties().apply {
                setProperty("søkersFødselsnummer", søknad.personIdent)
                setProperty("type", søknad.type.name)
            }
            return Task(TYPE, søknad.id.toString(), properties)
        }
    }
}
