package no.nav.tilleggsstonader.soknad.soknad.domene

import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.soknad.IntegrationTest
import no.nav.tilleggsstonader.soknad.infrastruktur.database.JsonWrapper
import no.nav.tilleggsstonader.soknad.infrastruktur.database.repository.findByIdOrThrow
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class SøknadRepositoryTest : IntegrationTest() {

    @Autowired
    lateinit var søknadRepository: SøknadRepository

    @Test
    fun `skal kunne lagre og hente søknad`() {
        val søknad = lagreSøknad()
        assertThat(søknadRepository.findByIdOrThrow(søknad.id)).isEqualTo(søknad)
    }

    private fun lagreSøknad() = søknadRepository.insert(
        Søknad(
            type = Stønadstype.BARNETILSYN,
            personIdent = "123",
            søknadJson = JsonWrapper("{}"),
        ),
    )
}
