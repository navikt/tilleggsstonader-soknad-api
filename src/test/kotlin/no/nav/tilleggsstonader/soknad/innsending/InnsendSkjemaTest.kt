package no.nav.tilleggsstonader.soknad.innsending

import io.mockk.verify
import no.nav.tilleggsstonader.kontrakter.dokarkiv.ArkiverDokumentRequest
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokumenttype
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Filtype
import no.nav.tilleggsstonader.kontrakter.felles.Skjematype
import no.nav.tilleggsstonader.soknad.IntegrationTest
import no.nav.tilleggsstonader.soknad.integrasjonstest.extensions.kall.sendInnKjøreliste
import no.nav.tilleggsstonader.soknad.integrasjonstest.extensions.kall.sendInnSøknadBarnetilsyn
import no.nav.tilleggsstonader.soknad.integrasjonstest.extensions.kall.sendInnSøknadLæremidler
import no.nav.tilleggsstonader.soknad.integrasjonstest.extensions.tasks.kjørTasksKlareForProsesseringTilIngenTasksIgjen
import no.nav.tilleggsstonader.soknad.kjøreliste.KjørelisteTestdata
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.SøknadBarnetilsynUtil
import no.nav.tilleggsstonader.soknad.soknad.domene.SkjemaRepository
import no.nav.tilleggsstonader.soknad.soknad.læremidler.SøknadLæremidlerUtil
import org.apache.kafka.clients.producer.ProducerRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import java.time.LocalDate

class InnsendSkjemaTest : IntegrationTest() {
    @Autowired
    lateinit var skjemaRepository: SkjemaRepository

    @Autowired
    lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @Test
    fun `innsending av kjøreliste`() {
        val kjøreliste = KjørelisteTestdata.kjørelisteDto()
        val response = sendInnKjøreliste(kjøreliste)

        assertThat(response.mottattTidspunkt.toLocalDate()).isEqualTo(LocalDate.now())

        validerSkjemaHarBlittSendtInnOgProsessert(Skjematype.DAGLIG_REISE_KJØRELISTE, Dokumenttype.DAGLIG_REISE_TSO_KJØRELISTE)
    }

    @Test
    fun `innsending av søknad læremidler`() {
        val søknadLæremidler = SøknadLæremidlerUtil.søknadLæremidler
        val response = sendInnSøknadLæremidler(søknadLæremidler)

        assertThat(response.mottattTidspunkt.toLocalDate()).isEqualTo(LocalDate.now())

        validerSkjemaHarBlittSendtInnOgProsessert(Skjematype.SØKNAD_LÆREMIDLER, Dokumenttype.LÆREMIDLER_SØKNAD)
    }

    @Test
    fun `innsending av søknad barnetilsyn`() {
        val søknadBarnetilsyn = SøknadBarnetilsynUtil.søknadBarnetilsyn
        val response = sendInnSøknadBarnetilsyn(søknadBarnetilsyn)

        assertThat(response.mottattTidspunkt.toLocalDate()).isEqualTo(LocalDate.now())

        validerSkjemaHarBlittSendtInnOgProsessert(Skjematype.SØKNAD_BARNETILSYN, Dokumenttype.BARNETILSYN_SØKNAD)
    }

    private fun validerSkjemaHarBlittSendtInnOgProsessert(
        skjematype: Skjematype,
        dokumenttype: Dokumenttype,
    ) {
        kjørTasksKlareForProsesseringTilIngenTasksIgjen()

        // Verifiserer at skjema har blitt lagret i databasen
        val skjema =
            with(skjemaRepository.findAll()) {
                assertThat(this).hasSize(1)
                this.single()
            }

        assertThat(skjema.type).isEqualTo(skjematype)

        // Verifiserer at det har blitt opprettet journalpost av skjema
        val slot = io.mockk.slot<ArkiverDokumentRequest>()
        verify { integrasjonerClient.arkiver(capture(slot)) }

        val arkiverDokumentRequest = slot.captured
        assertThat(arkiverDokumentRequest.eksternReferanseId).isEqualTo(skjema.id.toString())

        val hoveddokumentvarianter = arkiverDokumentRequest.hoveddokumentvarianter
        assertThat(hoveddokumentvarianter.map { it.filtype }).containsExactlyInAnyOrder(Filtype.PDFA, Filtype.JSON)
        assertThat(hoveddokumentvarianter.map { it.dokumenttype }.distinct().single()).isEqualTo(dokumenttype)

        // Verifiserer at det har blitt sendt varsel til dittnav
        val sendteVarslerDittNav = mutableListOf<ProducerRecord<String, String>>()
        verify { kafkaTemplate.send(capture(sendteVarslerDittNav)) }
        assertThat(sendteVarslerDittNav.filter { it.key() == skjema.id.toString() }).hasSize(1)
    }
}
