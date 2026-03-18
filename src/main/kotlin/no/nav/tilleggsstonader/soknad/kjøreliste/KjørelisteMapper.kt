package no.nav.tilleggsstonader.soknad.kjøreliste

import no.nav.tilleggsstonader.kontrakter.felles.Språkkode
import no.nav.tilleggsstonader.kontrakter.søknad.InnsendtSkjema
import no.nav.tilleggsstonader.kontrakter.søknad.KjørelisteSkjema
import no.nav.tilleggsstonader.kontrakter.søknad.Reisedag
import no.nav.tilleggsstonader.kontrakter.søknad.UkeMedReisedager
import no.nav.tilleggsstonader.soknad.soknad.SøknadMetadataDto
import java.time.LocalDateTime

object KjørelisteMapper {
    fun map(
        ident: String,
        mottattTidspunkt: LocalDateTime,
        dto: KjørelisteDto,
    ): InnsendtSkjema<KjørelisteSkjema> {
        val språkkode = Språkkode.NB
        return InnsendtSkjema(
            ident = ident,
            mottattTidspunkt = mottattTidspunkt,
            språk = språkkode,
            skjema =
                KjørelisteSkjema(
                    reiseId = dto.reiseId,
                    reisedagerPerUkeAvsnitt = dto.reisedagerPerUkeAvsnitt.map { it.mapTilSkjema() },
                    dokumentasjon = dto.dokumentasjon,
                ),
        )
    }
}

private fun UkeMedReisedagerDto.mapTilSkjema(): UkeMedReisedager =
    UkeMedReisedager(
        ukeLabel = ukeLabel,
        spørsmål = spørsmål,
        reisedager =
            reisedager.map {
                Reisedag(
                    dato = it.dato,
                    harKjørt = it.harKjørt,
                    parkeringsutgift = it.parkeringsutgift,
                )
            },
    )

fun KjørelisteSkjema.tilDto(): KjørelisteDto =
    KjørelisteDto(
        reiseId = reiseId,
        reisedagerPerUkeAvsnitt = reisedagerPerUkeAvsnitt.map { it.tilDto() },
        dokumentasjon = dokumentasjon,
        søknadMetadata = SøknadMetadataDto(søknadFrontendGitHash = null),
    )

private fun UkeMedReisedager.tilDto(): UkeMedReisedagerDto =
    UkeMedReisedagerDto(
        ukeLabel = ukeLabel,
        spørsmål = spørsmål,
        reisedager =
            reisedager.map {
                ReisedagDto(
                    dato = it.dato,
                    harKjørt = it.harKjørt,
                    parkeringsutgift = it.parkeringsutgift,
                )
            },
    )
