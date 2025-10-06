package no.nav.tilleggsstonader.soknad.soknad.domene

import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.kontrakter.søknad.Vedleggstype
import no.nav.tilleggsstonader.soknad.IntegrationTest
import no.nav.tilleggsstonader.soknad.infrastruktur.database.JsonWrapper
import no.nav.tilleggsstonader.soknad.infrastruktur.database.repository.findByIdOrThrow
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class VedleggRepositoryTest : IntegrationTest() {
    @Autowired
    lateinit var søknadRepository: SøknadRepository

    @Autowired
    lateinit var vedleggRepository: VedleggRepository

    @Test
    fun `skal kunne lagre og hente vedlegg`() {
        val søknad = lagreSøknad()
        val vedlegg = lagreVedlegg(søknad)
        assertThat(vedleggRepository.findByIdOrThrow(vedlegg.id)).isEqualTo(vedlegg)
    }

    @Nested
    inner class FindBySøknadId {
        @Test
        fun `skal finne vedlegg til søknad`() {
            val søknad = lagreSøknad()
            val søknad2 = lagreSøknad()
            val vedlegg = lagreVedlegg(søknad)
            val vedlegg2 = lagreVedlegg(søknad)
            val vedlegg3 = lagreVedlegg(søknad2)

            assertThat(vedleggRepository.findBySøknadId(søknad.id).map { it.id })
                .containsExactlyInAnyOrder(vedlegg.id, vedlegg2.id)

            assertThat(vedleggRepository.findBySøknadId(søknad2.id).map { it.id })
                .containsExactlyInAnyOrder(vedlegg3.id)
        }
    }

    private fun lagreVedlegg(skjema: Skjema) =
        vedleggRepository.insert(
            Vedlegg(
                id = UUID.randomUUID(),
                søknadId = skjema.id,
                type = Vedleggstype.UTGIFTER_PASS_SFO_AKS_BARNEHAGE,
                navn = "charlie.pdf",
                innhold = byteArrayOf(13),
            ),
        )

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
