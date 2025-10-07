package no.nav.tilleggsstonader.soknad.prosessering

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.soknad.infrastruktur.database.JsonWrapper
import no.nav.tilleggsstonader.soknad.soknad.SkjemaService
import no.nav.tilleggsstonader.soknad.soknad.domene.Skjema
import no.nav.tilleggsstonader.soknad.varsel.DittNavKafkaProducer
import org.junit.jupiter.api.Test
import java.util.UUID

class SendNotifikasjonTaskTest {
    private val skjemaService = mockk<SkjemaService>()
    private val dittNavKafkaProducer = mockk<DittNavKafkaProducer>(relaxed = true)
    private val sendNotifikasjonTask = SendNotifikasjonTask(dittNavKafkaProducer, skjemaService)

    @Test
    fun `Task blir kjørt for å sende notifikasjon om mottatt søknad om tilsyn barn`() {
        val søknadBarnetilsynSkjema = opprettSkjema(Stønadstype.BARNETILSYN)
        every { skjemaService.hentSkjema(UUID.fromString(SKJEMA_ID)) } returns søknadBarnetilsynSkjema
        sendNotifikasjonTask.doTask(SendNotifikasjonTask.opprettTask(søknadBarnetilsynSkjema))
        verifiserForventetKallMed("Vi har mottatt søknaden din om pass av barn.")
    }

    @Test
    fun `Task blir kjørt for å sende notifikasjon om mottatt søknad om læremidler`() {
        val søknadLæremidlerSkjema = opprettSkjema(Stønadstype.LÆREMIDLER)
        every { skjemaService.hentSkjema(UUID.fromString(SKJEMA_ID)) } returns søknadLæremidlerSkjema
        sendNotifikasjonTask.doTask(SendNotifikasjonTask.opprettTask(søknadLæremidlerSkjema))
        verifiserForventetKallMed("Vi har mottatt søknaden din om læremidler.")
    }

    private fun verifiserForventetKallMed(forventetTekst: String) {
        verify(exactly = 1) {
            skjemaService.hentSkjema(UUID.fromString(SKJEMA_ID))
            dittNavKafkaProducer.sendToKafka(
                FNR,
                forventetTekst,
                SKJEMA_ID,
            )
        }
    }

    private fun opprettSkjema(type: Stønadstype): Skjema =
        Skjema(
            id = UUID.fromString(SKJEMA_ID),
            skjemaJson = JsonWrapper(""),
            type = type,
            personIdent = FNR,
            frontendGitHash = "aabbccd",
        )

    companion object {
        private const val FNR = "12345678901"
        private const val SKJEMA_ID = "e8703be6-eb47-476a-ae52-096df47430d6"
    }
}
