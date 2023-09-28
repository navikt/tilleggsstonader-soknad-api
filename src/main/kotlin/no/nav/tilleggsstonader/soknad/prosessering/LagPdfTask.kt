package no.nav.tilleggsstonader.soknad.prosessering

import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import no.nav.tilleggsstonader.soknad.dokument.PdfService
import no.nav.tilleggsstonader.soknad.soknad.domene.Søknad
import org.springframework.stereotype.Service
import java.util.Properties
import java.util.UUID

@Service
@TaskStepBeskrivelse(taskStepType = ArkiverSøknadTask.TYPE, beskrivelse = "Arkiver søknad")
class LagPdfTask(
    private val pdfService: PdfService,
    private val taskService: TaskService,
) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val søknadId = UUID.fromString(task.payload)
        pdfService.lagPdf(søknadId)
    }

    override fun onCompletion(task: Task) {
        taskService.save(ArkiverSøknadTask.opprettTask(task))
    }

    companion object {
        const val TYPE = "LAG_PDF"
        fun opprettTask(søknad: Søknad): Task {
            val properties = Properties().apply {
                setProperty("søkersFødselsnummer", søknad.personIdent)
                setProperty("type", søknad.type.name)
            }
            return Task(TYPE, søknad.id.toString(), properties)
        }
    }
}
