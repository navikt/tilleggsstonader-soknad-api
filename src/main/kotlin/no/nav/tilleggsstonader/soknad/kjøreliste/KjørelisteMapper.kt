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
        reisedagerLabel = reisedagerLabel,
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
        reisedagerLabel = reisedagerLabel,
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

fun flettKjørelister(
    skjemaKjørelister: List<KjørelisteSkjema>,
    manueltRegistrertKjøreliste: ManueltRegistrertKjøreliste,
): KjørelisteVisningDto {
    val kjørelisteFraSkjema =
        skjemaKjørelister
            .flatMap { it.reisedagerPerUkeAvsnitt }
            .flatMap { it.reisedager }
            .associate { dag ->
                dag.dato.verdi to
                    ReisedagVisningDto(
                        dato = dag.dato.verdi,
                        harKjørt = dag.harKjørt,
                        parkeringsutgift = dag.parkeringsutgift.verdi?.toInt(),
                    )
            }

    val manueltRegistrertKjørelisteFraSak =
        manueltRegistrertKjøreliste.reisedager
            .associate { dag ->
                dag.dato to
                    ReisedagVisningDto(
                        dato = dag.dato,
                        harKjørt = dag.harKjørt,
                        parkeringsutgift = dag.parkeringsutgift,
                    )
            }

    return KjørelisteVisningDto(reisedager = (kjørelisteFraSkjema + manueltRegistrertKjørelisteFraSak).values.sortedBy { it.dato })
}
