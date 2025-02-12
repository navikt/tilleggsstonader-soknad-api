package no.nav.tilleggsstonader.soknad.prosessering

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Properties

internal class ArkiverSøknadTaskTest {
    private val taskService: TaskService = mockk()
    private val arkiverSøknadTask: ArkiverSøknadTask = ArkiverSøknadTask(mockk(), taskService)

    @Test
    fun `Skal gå til SendTilSaksbehandlingTask når ArkiverSøknadTask er utført`() {
        val slot = slot<Task>()
        every {
            taskService.save(capture(slot))
        } answers {
            slot.captured
        }

        arkiverSøknadTask.onCompletion(Task(type = "", payload = "", properties = Properties()))

        assertEquals(SendTilSaksbehandlingTask.TYPE, slot.captured.type)
    }
}
