package no.nav.tilleggsstonader.soknad.integrasjonstest.extensions.tasks

import no.nav.familie.prosessering.domene.Task
import no.nav.tilleggsstonader.soknad.IntegrationTest
import org.springframework.data.domain.Pageable

fun IntegrationTest.kjørTasksKlareForProsesseringTilIngenTasksIgjen() {
    do {
        kjørTasksKlareForProsessering()
    } while (taskService.finnAlleTasksKlareForProsessering(Pageable.unpaged()).isNotEmpty())
}

fun IntegrationTest.kjørTasksKlareForProsessering() {
    logger.info("Kjører tasks klare for prosessering")
    taskService
        .finnAlleTasksKlareForProsessering(Pageable.unpaged())
        .forEach { kjørTask(it) }
    logger.info("Tasks kjørt OK")
}

fun IntegrationTest.kjørTask(task: Task) {
    try {
        taskWorker.markerPlukket(task.id)
        logger.info("Kjører task ${task.id} type=${task.type} payload=${task.payload}")
        taskWorker.doActualWork(task.id)
    } catch (e: Exception) {
        logger.error("Feil ved kjøring av task ${task.id} type=${task.type} payload=${task.payload}", e)
    }
}
