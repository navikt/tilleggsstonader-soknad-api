package no.nav.tilleggsstonader.soknad.integrasjonstest.extensions.kall

import no.nav.tilleggsstonader.kontrakter.felles.Skjematype
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.soknad.IntegrationTest
import no.nav.tilleggsstonader.soknad.person.dto.PersonMedBarnDto
import org.springframework.test.web.servlet.client.expectBody

fun IntegrationTest.harBehandlingKall(
    personident: String,
    stønadstype: Stønadstype? = null,
    skjematype: Skjematype? = null,
) = restTestClient
    .get()
    .uri { builder ->
        builder.path("/api/person/har-behandling")
            .apply { stønadstype?.let { queryParam("stonadstype", it.name) } }
            .apply { skjematype?.let { queryParam("skjematype", it.name) } }
            .build()
    }
    .medSøkerBearerToken(personident)
    .exchange()

fun IntegrationTest.harBehandling(
    personident: String,
    stønadstype: Stønadstype? = null,
    skjematype: Skjematype? = null,
) = harBehandlingKall(personident, stønadstype, skjematype)
    .expectStatus()
    .isOk
    .expectBody<Boolean>()
    .returnResult()
    .responseBody!!

fun IntegrationTest.hentPersonKall(personident: String?) =
    restTestClient
        .get()
        .uri("/api/person")
        .let { if (personident != null) it.medSøkerBearerToken(personident) else it }
        .exchange()

fun IntegrationTest.hentPerson(personident: String?) =
    hentPersonKall(personident)
        .expectStatus()
        .isOk
        .expectBody<PersonMedBarnDto>()
        .returnResult()
        .responseBody!!

fun IntegrationTest.hentPersonMedBarn(personident: String?) =
    restTestClient
        .get()
        .uri("/api/person/med-barn")
        .let { if (personident != null) it.medSøkerBearerToken(personident) else it }
        .exchange()
        .expectStatus()
        .isOk
        .expectBody<PersonMedBarnDto>()
        .returnResult()
        .responseBody!!
