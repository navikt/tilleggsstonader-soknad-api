package no.nav.tilleggsstonader.soknad.util

import no.nav.tilleggsstonader.kontrakter.aktivitet.AktivitetArenaDto
import no.nav.tilleggsstonader.kontrakter.aktivitet.Kilde
import no.nav.tilleggsstonader.kontrakter.aktivitet.StatusAktivitet
import no.nav.tilleggsstonader.kontrakter.aktivitet.TypeAktivitet
import no.nav.tilleggsstonader.libs.utils.osloDateNow
import java.math.BigDecimal

object AktivitetArenaDtoUtil {
    fun aktivitetArenaDto(
        id: String,
        erStønadsberettiget: Boolean = true,
        type: TypeAktivitet = TypeAktivitet.JOBBK,
    ) = AktivitetArenaDto(
        id = id,
        fom = osloDateNow(),
        tom = osloDateNow(),
        type = type.name,
        typeNavn = "Type navn",
        status = StatusAktivitet.AKTUELL,
        statusArena = "BEHOV",
        antallDagerPerUke = 5,
        prosentDeltakelse = BigDecimal("100"),
        erStønadsberettiget = erStønadsberettiget,
        erUtdanning = false,
        arrangør = "Baker AS",
        kilde = Kilde.ARENA,
    )
}
