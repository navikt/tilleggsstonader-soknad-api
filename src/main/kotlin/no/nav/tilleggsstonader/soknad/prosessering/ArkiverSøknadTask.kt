package no.nav.tilleggsstonader.soknad.prosessering

import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.tilleggsstonader.soknad.arkivering.ArkiveringService
import no.nav.tilleggsstonader.soknad.soknad.Søknad
import org.springframework.stereotype.Service
import java.util.Properties
import java.util.UUID

@Service
@TaskStepBeskrivelse(taskStepType = ArkiverSøknadTask.TYPE, beskrivelse = "Arkiver søknad")
class ArkiverSøknadTask(
    private val arkiveringService: ArkiveringService
) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val søknadId = UUID.fromString(task.payload)
        arkiveringService.journalførSøknad(søknadId, task.callId)
    }

    companion object {
        const val TYPE = "arkiverSøknad"

        fun opprettTask(søknad: Søknad): Task {
            val properties = Properties().apply {
                setProperty("søkersFødselsnummer", søknad.personIdent)
                setProperty("type", søknad.type.name)
            }
            return Task(TYPE, søknad.id.toString(), properties)
        }
    }
}