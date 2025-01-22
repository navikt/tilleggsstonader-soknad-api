package no.nav.tilleggsstonader.soknad.aktivitet

import io.mockk.every
import io.mockk.verify
import no.nav.tilleggsstonader.kontrakter.aktivitet.TypeAktivitet
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.soknad.IntegrationTest
import no.nav.tilleggsstonader.soknad.infrastruktur.AktivitetClientConfig.Companion.resetMock
import no.nav.tilleggsstonader.soknad.util.AktivitetArenaDtoUtil.aktivitetArenaDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class AktivitetServiceTest : IntegrationTest() {

    @Autowired
    lateinit var aktivitetClient: AktivitetClient

    @Autowired
    lateinit var aktivitetService: AktivitetService

    @AfterEach
    override fun tearDown() {
        super.tearDown()
        resetMock(aktivitetClient)
    }

    @Test
    fun `skal cachea svaret fra client`() {
        every { aktivitetClient.hentAktiviteter("1", any(), any()) } returns listOf(aktivitetArenaDto("10"))
        every { aktivitetClient.hentAktiviteter("2", any(), any()) } returns listOf(aktivitetArenaDto("20"))

        aktivitetService.hentAktiviteter("1", Stønadstype.BARNETILSYN)
        val aktivitetIdent1 = aktivitetService.hentAktiviteter("1", Stønadstype.BARNETILSYN)
        aktivitetService.hentAktiviteter("2", Stønadstype.BARNETILSYN)
        val aktivitetIdent2 = aktivitetService.hentAktiviteter("2", Stønadstype.BARNETILSYN)

        assertThat(aktivitetIdent1.single().id).isEqualTo("10")
        assertThat(aktivitetIdent2.single().id).isEqualTo("20")

        verify(exactly = 1) { aktivitetClient.hentAktiviteter("1", any(), any()) }
        verify(exactly = 1) { aktivitetClient.hentAktiviteter("2", any(), any()) }
    }

    @Test
    fun `skal ikke returnere typer som ikke gir rett på stønaden`() {
        val type = TypeAktivitet.FLEKSJOBB
        every { aktivitetClient.hentAktiviteter("1", any(), any()) } returns listOf(
            aktivitetArenaDto(
                "10",
                type = type,
                erStønadsberettiget = false,
            ),
        )

        val aktiviteter = aktivitetService.hentAktiviteter("1", Stønadstype.BARNETILSYN)
        assertThat(aktiviteter).isEmpty()
    }
}
