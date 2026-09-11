package no.nav.tilleggsstonader.soknad.kjøreliste

import no.nav.tilleggsstonader.kontrakter.søknad.Dokument
import no.nav.tilleggsstonader.soknad.IntegrationTest
import no.nav.tilleggsstonader.soknad.infrastruktur.DagligReisePrivatBilClientConfig
import no.nav.tilleggsstonader.soknad.sak.DagligReisePrivatBilClient
import no.nav.tilleggsstonader.soknad.soknad.domene.SkjemaRepository
import no.nav.tilleggsstonader.soknad.tokenSubject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Gjenskaper produksjonshendelsen der en bruker sendte inn to eksakt like kjørelister
 * med kun millisekunders mellomrom - sannsynligvis pga. en bug i frontend (f.eks.
 * manglende debounce/disable på submit-knapp).
 *
 * Rotårsak: `KjørelisteService.validerKjøreliste` leser "allerede innsendte uker" fra
 * databasen FØR noen av de to samtidige forsøkene har rukket å lagre sitt skjema.
 * Begge kan derfor validere OK og lagres som separate skjema - dobbel PDF, dobbel
 * journalpost og dobbelt varsel til bruker.
 *
 * Denne testen SKAL feile helt til rød-sone-oppgaven (advisory lock i
 * `KjørelisteService.mottaKjøreliste`, se TODO der) er implementert av utvikler.
 * Når låsen er på plass skal nøyaktig ett av de to samtidige forsøkene lykkes, og
 * det andre skal feile med "allerede sendt inn"-valideringsfeilen.
 */
class KjørelisteRaceConditionTest : IntegrationTest() {
    @Autowired
    private lateinit var dagligReisePrivatBilClient: DagligReisePrivatBilClient

    @Autowired
    private lateinit var skjemaRepository: SkjemaRepository

    @BeforeEach
    fun resetKjørelisteMock() {
        DagligReisePrivatBilClientConfig.resetMock(dagligReisePrivatBilClient)
    }

    @Test
    fun `skal ikke lagre duplikat når to identiske kjørelister sendes inn samtidig`() {
        // Egne vedlegg-id-er per forsøk for å isolere race conditionen i valider+lagre-flyten
        // fra vedlegg-tabellens (fra før eksisterende, urelaterte) unike id-constraint - i
        // praksis ville en reell dobbel-innsending fra frontend uansett generert nye
        // vedlegg-referanser per opplasting.
        val kjørelisteForsøk =
            (1..2).map {
                KjørelisteTestdata.kjørelisteDto().copy(
                    dokumentasjon =
                        listOf(
                            lagDokumentasjonFelt().copy(
                                opplastedeVedlegg = listOf(Dokument(id = UUID.randomUUID(), navn = "Parkering 1. juni")),
                            ),
                        ),
                )
            }

        val antallSamtidigeForsøk = kjørelisteForsøk.size
        val startSignal = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(antallSamtidigeForsøk)
        try {
            val futures =
                kjørelisteForsøk.map { kjøreliste ->
                    executor.submit<Int> {
                        startSignal.await()
                        // Samme personident (tokenSubject) for begge kall - avgjørende for å
                        // faktisk gjenskape at det er SAMME bruker som sender inn to ganger.
                        restTestClient
                            .post()
                            .uri("/api/kjorelister")
                            .body(kjøreliste)
                            .medSøkerBearerToken(tokenSubject)
                            .exchange()
                            .returnResult(Void::class.java)
                            .status
                            .value()
                    }
                }

            startSignal.countDown()
            val statusKoder = futures.map { it.get(30, TimeUnit.SECONDS) }

            // Forventet sluttilstand når rød-sone-oppgaven er løst: ett forsøk lykkes (200),
            // det andre feiler på eksisterende "allerede sendt inn"-validering (400).
            assertThat(statusKoder).containsExactlyInAnyOrder(200, 400)

            // Uansett hvilke statuskoder som kommer tilbake skal aldri mer enn ett
            // skjema faktisk være lagret i databasen for denne innsendingen.
            assertThat(skjemaRepository.findAll()).hasSize(1)
        } finally {
            executor.shutdown()
        }
    }
}
