package no.nav.tilleggsstonader.soknad.aktivitet

import io.mockk.every
import io.mockk.verify
import no.nav.tilleggsstonader.soknad.IntegrationTest
import no.nav.tilleggsstonader.soknad.infrastruktur.AktivitetClientConfig.Companion.resetMock
import no.nav.tilleggsstonader.soknad.util.AktivitetArenaDtoUtil.aktivitetArenaDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class AktivitetServiceTest : IntegrationTest() {

    @Autowired
    lateinit var aktivitetClient: AktivitetClient

    @Autowired
    lateinit var aktivitetService: AktivitetService

    @BeforeEach
    fun setUp() {
        every { aktivitetClient.hentAKtiviteter("1", any(), any()) } returns listOf(aktivitetArenaDto("10"))
        every { aktivitetClient.hentAKtiviteter("2", any(), any()) } returns listOf(aktivitetArenaDto("20"))
    }

    @AfterEach
    override fun tearDown() {
        resetMock(aktivitetClient)
    }

    @Test
    fun `skal cachea svaret fra client`() {
        aktivitetService.hentAktiviteter("1")
        val aktivitetIdent1 = aktivitetService.hentAktiviteter("1")
        aktivitetService.hentAktiviteter("2")
        val aktivitetIdent2 = aktivitetService.hentAktiviteter("2")

        assertThat(aktivitetIdent1.single().id).isEqualTo("10")
        assertThat(aktivitetIdent2.single().id).isEqualTo("20")

        verify(exactly = 1) { aktivitetClient.hentAKtiviteter("1", any(), any()) }
        verify(exactly = 1) { aktivitetClient.hentAKtiviteter("2", any(), any()) }
    }
}
