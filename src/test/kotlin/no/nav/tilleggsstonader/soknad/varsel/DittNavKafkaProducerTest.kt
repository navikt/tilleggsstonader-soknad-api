package no.nav.tilleggsstonader.soknad.varsel

import io.mockk.mockk
import no.nav.tilleggsstonader.soknad.prosessering.SendNotifikasjonTask
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DittNavKafkaProducerTest {

    private lateinit var dittNavKafkaProducer: DittNavKafkaProducer
    private lateinit var sendNotifikasjonTask: SendNotifikasjonTask

    @BeforeEach
    fun setUp() {
        dittNavKafkaProducer = mockk(relaxed = true)
        sendNotifikasjonTask = SendNotifikasjonTask(dittNavKafkaProducer)
    }

    @Test
    fun `Kafkaevent inneholder beskjed`() {
        val beskjed = dittNavKafkaProducer.sendToKafka(
            eventId = EVENT_ID,
            fnr = "123",
            melding = "tekst",
        )
        Assertions.assertEquals(forventetJson, beskjed)
    }

    @Language("json")
    private val forventetJson = """
        
    """.trimIndent()

    companion object {
        private const val EVENT_ID = "e8703be6-eb47-476a-ae52-096df47430d7"
    }
}
