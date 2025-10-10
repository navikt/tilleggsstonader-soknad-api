package no.nav.tilleggsstonader.soknad.prosessering

import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.tilleggsstonader.kontrakter.felles.Skjematype
import no.nav.tilleggsstonader.soknad.soknad.SkjemaService
import no.nav.tilleggsstonader.soknad.soknad.domene.Skjema
import no.nav.tilleggsstonader.soknad.varsel.DittNavKafkaProducer
import org.springframework.stereotype.Service
import java.util.Properties
import java.util.UUID

@Service
@TaskStepBeskrivelse(taskStepType = SendNotifikasjonTask.TYPE, beskrivelse = "Send notifikasjon mottatt søknad")
class SendNotifikasjonTask(
    private val notifikasjonsService: DittNavKafkaProducer,
    private val skjemaService: SkjemaService,
) : AsyncTaskStep {
    override fun doTask(task: Task) {
        val skjema = skjemaService.hentSkjema(UUID.fromString(task.payload))
        val message = lagNotifikasjonsMelding(skjema.type)
        val eventId = skjema.id.toString()

        notifikasjonsService.sendToKafka(skjema.personIdent, message, eventId)
    }

    private fun lagNotifikasjonsMelding(skjematype: Skjematype): String =
        when (skjematype) {
            Skjematype.SØKNAD_BARNETILSYN -> "Vi har mottatt søknaden din om pass av barn."
            Skjematype.SØKNAD_LÆREMIDLER -> "Vi har mottatt søknaden din om læremidler."
            Skjematype.DAGLIG_REISE_KJØRELISTE -> "Vi har mottatt din kjøreliste"
            Skjematype.SØKNAD_BOUTGIFTER, Skjematype.SØKNAD_DAGLIG_REISE -> error("Håndterer ikke skjema $skjematype")
        }

    companion object {
        const val TYPE = "SEND_NOTIFIKASJON"

        fun opprettTask(skjema: Skjema): Task {
            val properties =
                Properties().apply {
                    setProperty("søkersFødselsnummer", skjema.personIdent)
                    setProperty("type", skjema.type.name)
                }
            return Task(TYPE, skjema.id.toString(), properties)
        }
    }
}
