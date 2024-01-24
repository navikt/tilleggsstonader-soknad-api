package no.nav.tilleggsstonader.soknad.prosessering

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.prosessering.domene.Task
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.soknad.infrastruktur.database.JsonWrapper
import no.nav.tilleggsstonader.soknad.soknad.SøknadService
import no.nav.tilleggsstonader.soknad.soknad.domene.Søknad
import no.nav.tilleggsstonader.soknad.varsel.DittNavKafkaProducer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class SendNotifikasjonTaskTest {
    private lateinit var task: Task

    @BeforeEach
    fun setUp() {
        val properties = Properties().apply {
            this["eventId"] = UUID.fromString(EVENT_ID)
        }
        task = Task(
            payload = EVENT_ID,
            type = "",
            properties = properties,
        )
    }
    private val søknadService = mockk<SøknadService>()
    private val dittNavKafkaProducer = mockk<DittNavKafkaProducer>(relaxed = true)
    private val sendNotifikasjonTask = SendNotifikasjonTask(dittNavKafkaProducer, søknadService)

    @Test
    fun `Task blir kjørt for å sende notifikasjon`() {
        mockSøknad()
        sendNotifikasjonTask.doTask(task)
        verifiserForventetKallMed("Vi har mottatt søknaden din om pass av barn.")
    }

    private fun verifiserForventetKallMed(forventetTekst: String) {
        verify(exactly = 1) {
            søknadService.hentSøknad(any())
            dittNavKafkaProducer.sendToKafka(
                FNR,
                forventetTekst,
                EVENT_ID,
            )
        }
    }

    private fun mockSøknad() {
        every { søknadService.hentSøknad(UUID.fromString(EVENT_ID)) } returns
            Søknad(
                id = UUID.fromString(SØKNAD_ID),
                søknadJson = JsonWrapper(""),
                type = Stønadstype.BARNETILSYN,
                personIdent = FNR,
            )
    }

    companion object {
        private const val FNR = "12345678901"
        private const val EVENT_ID = "e8703be6-eb47-476a-ae52-096df47430d7"
        private const val SØKNAD_ID = "e8703be6-eb47-476a-ae52-096df47430d6"
    }
}
