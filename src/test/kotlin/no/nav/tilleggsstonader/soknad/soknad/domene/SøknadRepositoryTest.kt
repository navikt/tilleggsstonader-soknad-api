package no.nav.tilleggsstonader.soknad.soknad.domene

import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
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
    lateinit var søknadRepository: SøknadRepository

    @Test
    fun `skal kunne lagre og hente søknad`() {
        val søknad = lagreSøknad()
        assertThat(søknadRepository.findByIdOrThrow(søknad.id)).isEqualTo(søknad)
    }

    @Test
    fun `skal ikke kunne lagre en søknad med samme versjon 2 ganger`() {
        val søknad = lagreSøknad()
        søknadRepository.update(søknad)
        assertThatThrownBy {
            søknadRepository.update(søknad)
        }.isInstanceOf(OptimisticLockingFailureException::class.java)
    }

    @Test
    fun `skal finne antall søknader per type`() {
        lagreSøknad()
        lagreSøknad()
        assertThat(søknadRepository.finnAntallPerType())
            .containsExactly(AntallPerType(Stønadstype.BARNETILSYN, 2))
    }

    @Test
    fun `skal kunne hente gammel søknad uten søknadFrontendGitHash`() {
        val skjema =
            søknadRepository.insert(
                Skjema(
                    type = Stønadstype.BARNETILSYN,
                    personIdent = "123",
                    søknadJson = JsonWrapper("{}"),
                    søknadFrontendGitHash = null,
                ),
            )

        assertThat(søknadRepository.findByIdOrThrow(skjema.id)).isEqualTo(skjema)
    }

    private fun lagreSøknad() =
        søknadRepository.insert(
            Skjema(
                type = Stønadstype.BARNETILSYN,
                personIdent = "123",
                søknadJson = JsonWrapper("{}"),
                søknadFrontendGitHash = "aabbccd",
            ),
        )
}
