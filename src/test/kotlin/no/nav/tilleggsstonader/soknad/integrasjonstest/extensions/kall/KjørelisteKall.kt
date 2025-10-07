package no.nav.tilleggsstonader.soknad.integrasjonstest.extensions.kall

import no.nav.tilleggsstonader.soknad.IntegrationTest
import no.nav.tilleggsstonader.soknad.kjøreliste.KjørelisteDto
import no.nav.tilleggsstonader.soknad.kjøreliste.KjørelisteResponse
import org.springframework.test.web.reactive.server.expectBody

fun IntegrationTest.sendInnKjørelisteKall(kjørelisteDto: KjørelisteDto) =
    webTestClient
        .post()
        .uri("/api/kjorelister")
        .bodyValue(kjørelisteDto)
        .medSøkerBearerToken()
        .exchange()

fun IntegrationTest.sendInnKjøreliste(kjørelisteDto: KjørelisteDto) =
    sendInnKjørelisteKall(kjørelisteDto)
        .expectStatus()
        .isOk
        .expectBody<KjørelisteResponse>()
        .returnResult()
        .responseBody!!
