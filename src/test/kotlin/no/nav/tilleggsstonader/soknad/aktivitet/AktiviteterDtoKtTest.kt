package no.nav.tilleggsstonader.soknad.aktivitet

import no.nav.tilleggsstonader.kontrakter.aktivitet.AktivitetArenaDto
import no.nav.tilleggsstonader.kontrakter.aktivitet.Kilde
import no.nav.tilleggsstonader.kontrakter.aktivitet.StatusAktivitet
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class AktiviteterDtoKtTest {

    @Nested
    inner class TilDto {

        @Test
        fun `happy case mapping`() {
            assertThat(dto().tilDto()).isEqualTo(
                AktivitetDto(
                    id = "1",
                    fom = LocalDate.now(),
                    tom = LocalDate.now().plusDays(1),
                    typeNavn = "typeNavn",
                    erUtdanning = true,
                    arrangør = "arrangør",
                ),
            )
        }

        @Test
        fun `returnerer null hvis fom mangler`() {
            assertThat(dto(fom = null).tilDto()).isNull()
        }

        @Test
        fun `returnerer null hvis erStønadsberettiget=false`() {
            assertThat(dto(erStønadsberettiget = false).tilDto()).isNull()
            assertThat(dto(erStønadsberettiget = null).tilDto()).isNull()
        }
    }

    fun dto(
        id: String = "1",
        fom: LocalDate? = LocalDate.now(),
        tom: LocalDate? = LocalDate.now().plusDays(1),
        type: String = "type",
        typeNavn: String = "typeNavn",
        status: StatusAktivitet? = null,
        statusArena: String? = null,
        antallDagerPerUke: Int? = null,
        prosentDeltakelse: BigDecimal? = null,
        erStønadsberettiget: Boolean? = true,
        erUtdanning: Boolean? = true,
        arrangør: String? = "arrangør",
        kilde: Kilde = Kilde.ARENA,
    ) = AktivitetArenaDto(
        id = id,
        fom = fom,
        tom = tom,
        type = type,
        typeNavn = typeNavn,
        status = status,
        statusArena = statusArena,
        antallDagerPerUke = antallDagerPerUke,
        prosentDeltakelse = prosentDeltakelse,
        erStønadsberettiget = erStønadsberettiget,
        erUtdanning = erUtdanning,
        arrangør = arrangør,
        kilde = kilde,
    )
}
