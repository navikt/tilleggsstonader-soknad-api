package no.nav.tilleggsstonader.soknad.aktivitet

import no.nav.tilleggsstonader.kontrakter.aktivitet.AktivitetArenaDto
import no.nav.tilleggsstonader.kontrakter.aktivitet.TypeAktivitet
import no.nav.tilleggsstonader.kontrakter.felles.Skjematype
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.kontrakter.felles.tilSkjematype
import java.time.LocalDate
import kotlin.Boolean

/**
 * @param aktiviteter er stønadsberrittgede aktiviteter
 * @param harAktiviteter sier om det finnes aktiviteter generellt, då [aktiviteter] er filtrerte aktiviteter
 */
data class AktiviteterDto(
    val aktiviteter: List<AktivitetDto>,
    val harAktiviteter: Boolean,
    val suksess: Boolean,
)

data class AktivitetDto(
    val id: String,
    val fom: LocalDate,
    val tom: LocalDate?,
    val typeNavn: String,
    val erUtdanning: Boolean,
    val erUtdanningPåVgsNivå: Boolean,
    val arrangør: String?,
)

fun AktivitetArenaDto.tilDto(): AktivitetDto? {
    val fom = this.fom ?: return null

    return AktivitetDto(
        id = id,
        fom = fom,
        tom = tom,
        typeNavn = typeNavn,
        erUtdanning = erUtdanning == true,
        erUtdanningPåVgsNivå = this.erUtdanningPåVgsNivå(),
        arrangør = arrangør,
    )
}

fun AktivitetArenaDto.erUtdanningPåVgsNivå(): Boolean {
    val typeAktiviteterPåVgsNivå =
        setOf(
            TypeAktivitet.GRUFAGYRKE.name,
            TypeAktivitet.ENKFAGYRKE.name,
            TypeAktivitet.OUTDEF.name,
        )

    return typeAktiviteterPåVgsNivå.contains(type)
}

/**
 * Kun de som er stønadsberettiget og gir rett til å søke på skal kunne søkes på
 * Av noen grunn får aktiviteter med status=fullført en tom status
 */
fun List<AktivitetArenaDto>.gjeldende() =
    this
        .filter { it.erStønadsberettiget == true }
        .filter { it.status == null || it.status?.rettTilÅSøke == true }

data class AktivitetRequest(
    val stønadstype: Stønadstype?,
    val skjematype: Skjematype?,
)

// TODO midlertidig mapping mens vi bytter fra stønadstype til skjematype i request.
fun AktivitetRequest.tilSkjematype(): Skjematype =
    when {
        skjematype != null -> skjematype
        stønadstype != null -> stønadstype.tilSkjematype()
        else -> throw IllegalArgumentException("Enten stønadstype eller skjematype må være satt")
    }

fun Skjematype.hentAktivitetAntallMånederTilbakeITid(): Long =
    when (this) {
        Skjematype.SØKNAD_BARNETILSYN -> 3
        Skjematype.SØKNAD_BOUTGIFTER -> 6
        Skjematype.SØKNAD_LÆREMIDLER -> 6
        Skjematype.SØKNAD_DAGLIG_REISE -> 3
        Skjematype.SØKNAD_REISE_TIL_SAMLING -> 3
        Skjematype.DAGLIG_REISE_KJØRELISTE -> error("Skjematype ${this.name} skal ikke brukes for å hente aktiviteter")
    }
