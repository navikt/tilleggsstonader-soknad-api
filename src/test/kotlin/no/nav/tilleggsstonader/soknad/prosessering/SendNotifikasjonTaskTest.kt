package no.nav.tilleggsstonader.soknad.prosessering

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.soknad.infrastruktur.database.JsonWrapper
import no.nav.tilleggsstonader.soknad.soknad.SøknadService
import no.nav.tilleggsstonader.soknad.soknad.domene.Søknad
import no.nav.tilleggsstonader.soknad.varsel.DittNavKafkaProducer
import org.junit.jupiter.api.Test
import java.util.*
import java.util.UUID.fromString

class SendNotifikasjonTaskTest {

    private val søknadService = mockk<SøknadService>()
    private val dittNavKafkaProducer = mockk<DittNavKafkaProducer>(relaxed = true)
    private val sendNotifikasjonTask = SendNotifikasjonTask(dittNavKafkaProducer, søknadService)

    @Test
    fun `Task blir kjørt for å sende notifikasjon om mottatt søknad om tilsyn barn`() {
        val søknad = opprettSøknad(Stønadstype.BARNETILSYN)
        every { søknadService.hentSøknad(fromString(SØKNAD_ID)) } returns søknad
        sendNotifikasjonTask.doTask(SendNotifikasjonTask.opprettTask(søknad))
        verifiserForventetKallMed("Vi har mottatt søknaden din om pass av barn.")
    }

    @Test
    fun `Task blir kjørt for å sende notifikasjon om mottatt søknad om læremidler`() {
        val søknad = opprettSøknad(Stønadstype.LÆREMIDLER)
        every { søknadService.hentSøknad(fromString(SØKNAD_ID)) } returns søknad
        sendNotifikasjonTask.doTask(SendNotifikasjonTask.opprettTask(søknad))
        verifiserForventetKallMed("Vi har mottatt søknaden din om læremidler.")
    }

    private fun verifiserForventetKallMed(forventetTekst: String) {
        verify(exactly = 1) {
            søknadService.hentSøknad(fromString(SØKNAD_ID))
            dittNavKafkaProducer.sendToKafka(
                FNR,
                forventetTekst,
                SØKNAD_ID,
            )
        }
    }

    private fun opprettSøknad(type: Stønadstype): Søknad {
        return Søknad(
            id = UUID.fromString(SØKNAD_ID),
            søknadJson = JsonWrapper(""),
            type = type,
            personIdent = FNR,
        )
    }

    companion object {
        private const val FNR = "12345678901"
        private const val SØKNAD_ID = "e8703be6-eb47-476a-ae52-096df47430d6"
    }
}
