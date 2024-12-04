package no.nav.tilleggsstonader.soknad.aktivitet

import no.nav.tilleggsstonader.kontrakter.aktivitet.AktivitetArenaDto
import no.nav.tilleggsstonader.kontrakter.aktivitet.Kilde
import no.nav.tilleggsstonader.kontrakter.aktivitet.StatusAktivitet
import no.nav.tilleggsstonader.libs.utils.osloDateNow
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
                    fom = osloDateNow(),
                    tom = osloDateNow().plusDays(1),
                    typeNavn = "typeNavn",
                    erUtdanning = true,
                    erUtdanningPåVgsNivå = false,
                    arrangør = "arrangør",
                ),
            )
        }

        @Test
        fun `returnerer null hvis fom mangler`() {
            assertThat(dto(fom = null).tilDto()).isNull()
        }
    }

    @Nested
    inner class Gjeldende {
        @Test
        fun `skal inneholde de som gir rett til tilleggsstømader`() {
            assertThat(listOf(dto(erStønadsberettiget = true)).gjeldende()).hasSize(1)
        }

        @Test
        fun `skal filtrere bort erStønadsberettiget=false eller null`() {
            assertThat(listOf(dto(erStønadsberettiget = false)).gjeldende()).isEmpty()
            assertThat(listOf(dto(erStønadsberettiget = null)).gjeldende()).isEmpty()
        }

        @Test
        fun `skal inneholde de som mangler status eller gir rett å søke på`() {
            assertThat(listOf(dto(status = null)).gjeldende()).hasSize(1)
            assertThat(listOf(dto(status = StatusAktivitet.AKTUELL)).gjeldende()).hasSize(1)
            assertThat(listOf(dto(status = StatusAktivitet.BEHOV)).gjeldende()).hasSize(1)
        }

        @Test
        fun `skal filtrere bort de som mangler status eller gir rett å søke på`() {
            assertThat(listOf(dto(status = StatusAktivitet.IKKE_AKTUELL)).gjeldende()).isEmpty()
            assertThat(listOf(dto(status = StatusAktivitet.FEILREGISTRERT)).gjeldende()).isEmpty()
            assertThat(listOf(dto(status = StatusAktivitet.PLANLAGT)).gjeldende()).isEmpty()
            assertThat(listOf(dto(status = StatusAktivitet.VENTELISTE)).gjeldende()).isEmpty()
        }
    }

    @Nested
    inner class `Er utdanning på vgs nivå` {
        @Test
        fun `skal returnere false hvis erUtdanning=false`() {
            assertThat(dto(erUtdanning = false).erUtdanningPåVgsNivå()).isFalse()
        }

        @Test
        fun `skal returnere false hvis type ikke er på vgs nivå`() {
            assertThat(dto(type = "type").erUtdanningPåVgsNivå()).isFalse()
        }

        @Test
        fun `skal returnere true hvis type er på vgs nivå`() {
            assertThat(dto(type = "GRUFAGYRKE", erUtdanning = false).erUtdanningPåVgsNivå()).isTrue()
            assertThat(dto(type = "ENKFAGYRKE", erUtdanning = false).erUtdanningPåVgsNivå()).isTrue()
            assertThat(dto(type = "OUTDEF").erUtdanningPåVgsNivå()).isTrue()
        }
    }

    fun dto(
        id: String = "1",
        fom: LocalDate? = osloDateNow(),
        tom: LocalDate? = osloDateNow().plusDays(1),
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
