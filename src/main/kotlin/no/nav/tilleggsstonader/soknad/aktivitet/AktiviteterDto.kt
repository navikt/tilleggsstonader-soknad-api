package no.nav.tilleggsstonader.soknad.aktivitet

import no.nav.tilleggsstonader.kontrakter.aktivitet.AktivitetArenaDto
import no.nav.tilleggsstonader.kontrakter.aktivitet.Kilde
import no.nav.tilleggsstonader.kontrakter.aktivitet.StatusAktivitet
import java.time.LocalDate

data class AktiviteterDto(
    val aktiviteter: List<AktivitetDto>,
    val suksess: Boolean,
)

data class AktivitetDto(
    val id: String,
    val fom: LocalDate?,
    val tom: LocalDate?,
    val type: String,
    val typeNavn: String,
    val status: StatusAktivitet?,
    val statusArena: String?,
    val erStønadsberettiget: Boolean?,
    val erUtdanning: Boolean?,
    val arrangør: String?,
    val kilde: Kilde,
)

fun AktivitetArenaDto.tilDto() = AktivitetDto(
    id = id,
    fom = fom,
    tom = tom,
    type = type,
    typeNavn = typeNavn,
    status = status,
    statusArena = statusArena,
    erStønadsberettiget = erStønadsberettiget,
    erUtdanning = erUtdanning,
    arrangør = arrangør,
    kilde = kilde,
)
