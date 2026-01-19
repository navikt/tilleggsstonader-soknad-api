package no.nav.tilleggsstonader.soknad.integrasjonstest.extensions.kall

import no.nav.tilleggsstonader.soknad.IntegrationTest
import no.nav.tilleggsstonader.soknad.soknad.Kvittering
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.SøknadBarnetilsynDto
import no.nav.tilleggsstonader.soknad.soknad.læremidler.SøknadLæremidlerDto
import no.nav.tilleggsstonader.soknad.tokenSubject
import org.springframework.test.web.servlet.client.expectBody

fun IntegrationTest.sendInnSøknadBarnetilsynKall(søknadBarnetilsynDto: SøknadBarnetilsynDto) =
    restTestClient
        .post()
        .uri("/api/soknad/pass-av-barn")
        .body(søknadBarnetilsynDto)
        .medSøkerBearerToken(tokenSubject)
        .exchange()

fun IntegrationTest.sendInnSøknadBarnetilsyn(søknadBarnetilsynDto: SøknadBarnetilsynDto) =
    sendInnSøknadBarnetilsynKall(søknadBarnetilsynDto)
        .expectStatus()
        .isOk
        .expectBody<Kvittering>()
        .returnResult()
        .responseBody!!

fun IntegrationTest.sendInnSøknadLæremidlerKall(
    søknadLæremidlerDto: SøknadLæremidlerDto,
    medToken: Boolean = true,
) = restTestClient
    .post()
    .uri("/api/soknad/laremidler")
    .body(søknadLæremidlerDto)
    .let {
        if (medToken) it.medSøkerBearerToken(tokenSubject) else it
    }.exchange()

fun IntegrationTest.sendInnSøknadLæremidler(søknadLæremidlerDto: SøknadLæremidlerDto) =
    sendInnSøknadLæremidlerKall(søknadLæremidlerDto)
        .expectStatus()
        .isOk
        .expectBody<Kvittering>()
        .returnResult()
        .responseBody!!
