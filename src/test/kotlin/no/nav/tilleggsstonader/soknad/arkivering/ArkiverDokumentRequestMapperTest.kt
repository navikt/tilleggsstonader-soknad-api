package no.nav.tilleggsstonader.soknad.arkivering

import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokumenttype
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Filtype
import no.nav.tilleggsstonader.kontrakter.felles.ObjectMapperProvider.jsonMapper
import no.nav.tilleggsstonader.kontrakter.felles.Skjematype
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.kontrakter.søknad.Vedleggstype
import no.nav.tilleggsstonader.soknad.arkivering.ArkiverDokumentRequestMapper.toDto
import no.nav.tilleggsstonader.soknad.infrastruktur.database.ByteArrayWrapper
import no.nav.tilleggsstonader.soknad.infrastruktur.database.JsonWrapper
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.SøknadBarnetilsynUtil
import no.nav.tilleggsstonader.soknad.soknad.domene.Skjema
import no.nav.tilleggsstonader.soknad.soknad.domene.Vedlegg
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

internal class ArkiverDokumentRequestMapperTest {
    @Test
    internal fun `barnetilsyn toDto - sjekk alle felt`() {
        val vedlegg = lagVedlegg()
        val skjema = lagSkjema(SøknadBarnetilsynUtil.søknadBarnetilsyn, Skjematype.SØKNAD_BARNETILSYN)

        val dto = toDto(skjema, Stønadstype.BARNETILSYN, listOf(vedlegg))

        assertThat(dto.fnr).isEqualTo(skjema.personIdent)
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

    private fun lagSkjema(
        skjema: Any,
        type: Skjematype,
    ) = Skjema(
        skjemaJson = JsonWrapper(jsonMapper.writeValueAsString(skjema)),
        personIdent = "123",
        type = type,
        skjemaPdf = ByteArrayWrapper(byteArrayOf(12)),
        frontendGitHash = "aabbccd",
    )

    private fun lagVedlegg() =
        Vedlegg(
            UUID.randomUUID(),
            UUID.randomUUID(),
            Vedleggstype.UTGIFTER_PASS_SFO_AKS_BARNEHAGE,
            "navn",
            ByteArrayWrapper(byteArrayOf(12)),
        )
}
