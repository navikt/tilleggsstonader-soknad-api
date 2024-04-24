package no.nav.tilleggsstonader.soknad.aktivitet

import no.nav.tilleggsstonader.kontrakter.aktivitet.AktivitetArenaDto
import java.time.LocalDate

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
        arrangør = arrangør,
    )
}

/**
 * Kun de som er stønadsberettiget og gir rett til å søke på skal kunne søkes på
 * Av noen grunn får aktiviteter med status=fullført en tom status
 */
fun List<AktivitetArenaDto>.gjeldende() = this
    .filter { it.erStønadsberettiget == true }
    .filter { it.status == null || it.status?.rettTilÅSøke == true }
