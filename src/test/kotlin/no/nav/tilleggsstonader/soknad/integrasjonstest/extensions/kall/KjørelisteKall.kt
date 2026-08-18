package no.nav.tilleggsstonader.soknad.integrasjonstest.extensions.kall

import no.nav.tilleggsstonader.soknad.IntegrationTest
import no.nav.tilleggsstonader.soknad.kjøreliste.KjørelisteDto
import no.nav.tilleggsstonader.soknad.kjøreliste.KjørelisteResponse
import no.nav.tilleggsstonader.soknad.kjøreliste.ManueltRegistrertKjøreliste
import no.nav.tilleggsstonader.soknad.tokenSubject
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

fun IntegrationTest.hentKjørelisterKall(
    reiseId: String,
    personident: String? = tokenSubject,
) = restTestClient
    .get()
    .uri("/api/kjorelister/$reiseId")
    .let { if (personident != null) it.medSøkerBearerToken(personident) else it }
    .exchange()

fun IntegrationTest.hentKjørelister(reiseId: String): ManueltRegistrertKjøreliste? =
    hentKjørelisterKall(reiseId)
        .expectStatus()
        .isOk
        .expectBody<ManueltRegistrertKjøreliste>()
        .returnResult()
        .responseBody
