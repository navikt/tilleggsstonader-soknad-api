package no.nav.tilleggsstonader.soknad.prosessering

import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import no.nav.tilleggsstonader.soknad.dokument.PdfService
import no.nav.tilleggsstonader.soknad.soknad.domene.Skjema
import org.springframework.stereotype.Service
import java.util.Properties
import java.util.UUID

@Service
@TaskStepBeskrivelse(taskStepType = LagPdfTask.TYPE, beskrivelse = "Lag pdf av søknaden, for etterlevelse")
class LagPdfTask(
    private val pdfService: PdfService,
    private val taskService: TaskService,
) : AsyncTaskStep {
    override fun doTask(task: Task) {
        val skjemaId = UUID.fromString(task.payload)
        pdfService.lagPdf(skjemaId)
    }

    override fun onCompletion(task: Task) {
        taskService.save(ArkiverSøknadTask.opprettTask(task))
    }

    companion object {
        const val TYPE = "LAG_PDF"

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
