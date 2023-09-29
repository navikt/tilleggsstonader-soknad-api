package no.nav.tilleggsstonader.soknad.prosessering

import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.tilleggsstonader.soknad.arkivering.ArkiveringService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(taskStepType = ArkiverSøknadTask.TYPE, beskrivelse = "Arkiver søknad")
class ArkiverSøknadTask(
    private val arkiveringService: ArkiveringService,
) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val søknadId = UUID.fromString(task.payload)
        arkiveringService.journalførSøknad(søknadId, task.callId)
    }

    companion object {
        const val TYPE = "ARKIVER_SØKNAD"

        fun opprettTask(task: Task): Task {
            return Task(TYPE, task.payload, task.metadata)
        }
    }
}
