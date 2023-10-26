package no.nav.tilleggsstonader.soknad.prosessering

import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.tilleggsstonader.soknad.sak.SendTilSaksbehandlingService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(taskStepType = LagPdfTask.TYPE, beskrivelse = "Send den arkiverte journalposten/søknad til sak for videre håndtering")
class SendTilSaksbehandlingTask(
    private val sendTilSaksbehandlingService: SendTilSaksbehandlingService,

) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val søknadId = UUID.fromString(task.payload)

        sendTilSaksbehandlingService.sendTilSak(søknadId)
    }

    companion object {
        const val TYPE = "SEND_TIL_SAKSBEHANDLING"

        fun opprettTask(task: Task): Task {
            return Task(TYPE, task.payload, task.metadata)
        }
    }
}
