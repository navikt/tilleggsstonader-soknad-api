package no.nav.tilleggsstonader.soknad.prosessering

import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.tilleggsstonader.soknad.soknad.SøknadService
import no.nav.tilleggsstonader.soknad.soknad.domene.Søknad
import no.nav.tilleggsstonader.soknad.varsel.DittNavKafkaProducer
import org.springframework.stereotype.Service
import java.util.*

@Service
@TaskStepBeskrivelse(taskStepType = SendNotifikasjonTask.TYPE, beskrivelse = "Send notifikasjon mottatt søknad")
class SendNotifikasjonTask(
    private val notifikasjonsService: DittNavKafkaProducer,
    private val søknadService: SøknadService,
) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val søknad = søknadService.hentSøknad(UUID.fromString(task.payload))
        val message = lagNotifikasjonsMelding()
        val eventId = task.metadata["eventId"].toString()

        notifikasjonsService.sendToKafka(søknad.personIdent, message, eventId)
    }

    private fun lagNotifikasjonsMelding(): String {
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
