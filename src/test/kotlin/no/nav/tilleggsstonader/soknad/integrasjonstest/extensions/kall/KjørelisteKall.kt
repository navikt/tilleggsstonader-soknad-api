package no.nav.tilleggsstonader.soknad.integrasjonstest.extensions.kall

import no.nav.tilleggsstonader.soknad.IntegrationTest
import no.nav.tilleggsstonader.soknad.kjøreliste.KjørelisteDto
import no.nav.tilleggsstonader.soknad.kjøreliste.KjørelisteResponse
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.test.web.servlet.client.expectBody

fun IntegrationTest.sendInnKjørelisteKall(kjørelisteDto: KjørelisteDto) =
    restTestClient
        .post()
        .uri("/api/kjorelister")
        .body(kjørelisteDto)
        .medSøkerBearerToken()
        .exchange()

fun IntegrationTest.sendInnKjøreliste(kjørelisteDto: KjørelisteDto) =
    sendInnKjørelisteKall(kjørelisteDto)
        .expectStatus()
        .isOk
        .expectBody<KjørelisteResponse>()
        .returnResult()
        .responseBody!!
