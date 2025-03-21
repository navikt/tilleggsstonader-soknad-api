package no.nav.tilleggsstonader.soknad.prosessering

import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import no.nav.tilleggsstonader.soknad.arkivering.ArkiveringService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(taskStepType = ArkiverSøknadTask.TYPE, beskrivelse = "Arkiver søknad")
class ArkiverSøknadTask(
    private val arkiveringService: ArkiveringService,
    private val taskService: TaskService,
) : AsyncTaskStep {
    override fun doTask(task: Task) {
        val søknadId = UUID.fromString(task.payload)
        arkiveringService.journalførSøknad(søknadId, task.callId)
    }

    override fun onCompletion(task: Task) {
        taskService.save(SendTilSaksbehandlingTask.opprettTask(task))
    }

    companion object {
        const val TYPE = "ARKIVER_SØKNAD"

        fun opprettTask(task: Task): Task = Task(TYPE, task.payload, task.metadata)
    }
}
