package no.nav.tilleggsstonader.soknad.soknad.domene

import no.nav.tilleggsstonader.kontrakter.felles.Skjematype
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
    lateinit var skjemaRepository: SkjemaRepository

    @Autowired
    lateinit var vedleggRepository: VedleggRepository

    @Test
    fun `skal kunne lagre og hente vedlegg`() {
        val skjema = lagreSkjema()
        val vedlegg = lagreVedlegg(skjema)
        assertThat(vedleggRepository.findByIdOrThrow(vedlegg.id)).isEqualTo(vedlegg)
    }

    @Nested
    inner class FindBySkjemaId {
        @Test
        fun `skal finne vedlegg til skjema`() {
            val skjema = lagreSkjema()
            val skjema2 = lagreSkjema()
            val vedlegg = lagreVedlegg(skjema)
            val vedlegg2 = lagreVedlegg(skjema)
            val vedlegg3 = lagreVedlegg(skjema2)

            assertThat(vedleggRepository.findBySkjemaId(skjema.id).map { it.id })
                .containsExactlyInAnyOrder(vedlegg.id, vedlegg2.id)

            assertThat(vedleggRepository.findBySkjemaId(skjema2.id).map { it.id })
                .containsExactlyInAnyOrder(vedlegg3.id)
        }
    }

    private fun lagreVedlegg(skjema: Skjema) =
        vedleggRepository.insert(
            Vedlegg(
                id = UUID.randomUUID(),
                skjemaId = skjema.id,
                type = Vedleggstype.UTGIFTER_PASS_SFO_AKS_BARNEHAGE,
                navn = "charlie.pdf",
                innhold = byteArrayOf(13),
            ),
        )

    private fun lagreSkjema() =
        skjemaRepository.insert(
            Skjema(
                type = Skjematype.SØKNAD_BARNETILSYN,
                personIdent = "123",
                skjemaJson = JsonWrapper("{}"),
                frontendGitHash = "aabbccd",
            ),
        )
}
