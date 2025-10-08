package no.nav.tilleggsstonader.soknad.soknad.domene

import no.nav.tilleggsstonader.kontrakter.felles.Skjematype
import no.nav.tilleggsstonader.soknad.IntegrationTest
import no.nav.tilleggsstonader.soknad.infrastruktur.database.JsonWrapper
import no.nav.tilleggsstonader.soknad.infrastruktur.database.repository.findByIdOrThrow
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.OptimisticLockingFailureException

class SøknadRepositoryTest : IntegrationTest() {
    @Autowired
    lateinit var skjemaRepository: SkjemaRepository

    @Test
    fun `skal kunne lagre og hente søknad`() {
        val søknad = lagreSøknad()
        assertThat(skjemaRepository.findByIdOrThrow(søknad.id)).isEqualTo(søknad)
    }

    @Test
    fun `skal ikke kunne lagre en søknad med samme versjon 2 ganger`() {
        val søknad = lagreSøknad()
        skjemaRepository.update(søknad)
        assertThatThrownBy {
            skjemaRepository.update(søknad)
        }.isInstanceOf(OptimisticLockingFailureException::class.java)
    }

    @Test
    fun `skal finne antall søknader per type`() {
        lagreSøknad()
        lagreSøknad()
        assertThat(skjemaRepository.finnAntallPerType())
            .containsExactly(AntallPerType(Skjematype.SØKNAD_BARNETILSYN, 2))
    }

    @Test
    fun `skal kunne hente gammel søknad uten søknadFrontendGitHash`() {
        val skjema =
            skjemaRepository.insert(
                Skjema(
                    type = Skjematype.SØKNAD_BARNETILSYN,
                    personIdent = "123",
                    skjemaJson = JsonWrapper("{}"),
                    frontendGitHash = null,
                ),
            )

        assertThat(skjemaRepository.findByIdOrThrow(skjema.id)).isEqualTo(skjema)
    }

    private fun lagreSøknad() =
        skjemaRepository.insert(
            Skjema(
                type = Skjematype.SØKNAD_BARNETILSYN,
                personIdent = "123",
                skjemaJson = JsonWrapper("{}"),
                frontendGitHash = "aabbccd",
            ),
        )
}
