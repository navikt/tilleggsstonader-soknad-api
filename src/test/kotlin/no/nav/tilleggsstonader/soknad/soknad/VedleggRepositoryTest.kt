package no.nav.tilleggsstonader.soknad.soknad

import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.soknad.IntegrationTest
import no.nav.tilleggsstonader.soknad.infrastruktur.database.JsonWrapper
import no.nav.tilleggsstonader.soknad.infrastruktur.database.repository.findByIdOrThrow
import no.nav.tilleggsstonader.soknad.soknad.domene.Søknad
import no.nav.tilleggsstonader.soknad.soknad.domene.SøknadRepository
import no.nav.tilleggsstonader.soknad.soknad.domene.Vedlegg
import no.nav.tilleggsstonader.soknad.soknad.domene.VedleggRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

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
    inner class findBySøknadId {

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

    private fun lagreVedlegg(søknad: Søknad) =
        vedleggRepository.insert(
            Vedlegg(
                søknadId = søknad.id,
                type = Vedleggstype.EKSEMPEL,
                navn = "charlie.pdf",
                innhold = byteArrayOf(13)
            )
        )

    private fun lagreSøknad() = søknadRepository.insert(
        Søknad(
            type = Stønadstype.BARNETILSYN,
            personIdent = "123",
            søknadJson = JsonWrapper("{}"),
        ),
    )
}