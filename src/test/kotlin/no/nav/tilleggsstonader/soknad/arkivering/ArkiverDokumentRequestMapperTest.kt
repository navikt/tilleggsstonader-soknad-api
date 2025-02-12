package no.nav.tilleggsstonader.soknad.arkivering

import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokumenttype
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Filtype
import no.nav.tilleggsstonader.kontrakter.felles.ObjectMapperProvider.objectMapper
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.kontrakter.søknad.Vedleggstype
import no.nav.tilleggsstonader.soknad.arkivering.ArkiverDokumentRequestMapper.toDto
import no.nav.tilleggsstonader.soknad.infrastruktur.database.JsonWrapper
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.SøknadBarnetilsynUtil
import no.nav.tilleggsstonader.soknad.soknad.domene.Søknad
import no.nav.tilleggsstonader.soknad.soknad.domene.Vedlegg
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

internal class ArkiverDokumentRequestMapperTest {
    @Test
    internal fun `barnetilsyn toDto - sjekk alle felt`() {
        val vedlegg = lagVedlegg()
        val søknad = lagSøknad(SøknadBarnetilsynUtil.søknad, Stønadstype.BARNETILSYN)

        val dto = toDto(søknad, listOf(vedlegg))

        assertThat(dto.fnr).isEqualTo(søknad.personIdent)
        assertThat(dto.forsøkFerdigstill).isFalse
        assertThat(dto.hoveddokumentvarianter).hasSize(2)

        assertThat(dto.hoveddokumentvarianter[0].dokumenttype.name).isEqualTo(Dokumenttype.BARNETILSYN_SØKNAD.name)
        assertThat(dto.hoveddokumentvarianter[0].tittel).isEqualTo(Dokumenttype.BARNETILSYN_SØKNAD.dokumentTittel())
        assertThat(dto.hoveddokumentvarianter[0].filtype).isEqualTo(Filtype.PDFA)

        assertThat(dto.hoveddokumentvarianter[1].dokumenttype.name).isEqualTo(Dokumenttype.BARNETILSYN_SØKNAD.name)
        assertThat(dto.hoveddokumentvarianter[1].tittel).isEqualTo(Dokumenttype.BARNETILSYN_SØKNAD.dokumentTittel())
        assertThat(dto.hoveddokumentvarianter[1].filtype).isEqualTo(Filtype.JSON)

        assertThat(dto.vedleggsdokumenter.first().dokumenttype).isEqualTo(Dokumenttype.BARNETILSYN_SØKNAD_VEDLEGG)
        assertThat(dto.vedleggsdokumenter.first().filnavn).isEqualTo(vedlegg.id.toString())
    }

    private fun lagSøknad(
        søknad: Any,
        type: Stønadstype,
    ) = Søknad(
        søknadJson = JsonWrapper(objectMapper.writeValueAsString(søknad)),
        personIdent = "123",
        type = type,
        søknadPdf = byteArrayOf(12),
    )

    private fun lagVedlegg() =
        Vedlegg(UUID.randomUUID(), UUID.randomUUID(), Vedleggstype.UTGIFTER_PASS_SFO_AKS_BARNEHAGE, "navn", byteArrayOf(12))
}
