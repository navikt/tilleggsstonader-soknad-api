package no.nav.tilleggsstonader.soknad.arkivering

import no.nav.tilleggsstonader.kontrakter.dokarkiv.ArkiverDokumentRequest
import no.nav.tilleggsstonader.kontrakter.dokarkiv.AvsenderMottaker
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokument
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokumenttype
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Filtype
import no.nav.tilleggsstonader.kontrakter.felles.BrukerIdType
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.soknad.soknad.domene.Søknad
import no.nav.tilleggsstonader.soknad.soknad.domene.Vedlegg

object ArkiverDokumentRequestMapper {

    fun toDto(
        søknad: Søknad,
        vedlegg: List<Vedlegg>,
    ): ArkiverDokumentRequest {
        val dokumenttype = typeHoveddokument(søknad.type)
        val søknadsdokumentJson =
            Dokument(
                søknad.søknadJson.json.toByteArray(),
                Filtype.JSON,
                null,
                dokumenttype.dokumentTittel(),
                dokumenttype,
            )
        val søknadsdokumentPdf =
            Dokument(søknad.søknadPdf!!, Filtype.PDFA, null, dokumenttype.dokumentTittel(), dokumenttype)
        return ArkiverDokumentRequest(
            fnr = søknad.personIdent,
            forsøkFerdigstill = false,
            hoveddokumentvarianter = listOf(søknadsdokumentPdf, søknadsdokumentJson),
            vedleggsdokumenter = mapVedlegg(vedlegg, søknad.type),
            eksternReferanseId = søknad.id.toString(),
            avsenderMottaker = AvsenderMottaker(id = søknad.personIdent, idType = BrukerIdType.FNR, navn = null),
        )
    }

    private fun typeHoveddokument(type: Stønadstype): Dokumenttype = when (type) {
        Stønadstype.BARNETILSYN -> Dokumenttype.BARNETILSYN_SØKNAD
    }

    private fun typeVedlegg(type: Stønadstype): Dokumenttype = when (type) {
        Stønadstype.BARNETILSYN -> Dokumenttype.BARNETILSYN_SØKNAD_VEDLEGG
    }

    private fun mapVedlegg(vedlegg: List<Vedlegg>, stønadstype: Stønadstype): List<Dokument> {
        if (vedlegg.isEmpty()) return emptyList()
        val dokumenttypeVedlegg = typeVedlegg(stønadstype)
        return vedlegg.map { tilDokument(it, dokumenttypeVedlegg) }
    }

    private fun tilDokument(vedlegg: Vedlegg, dokumenttypeVedlegg: Dokumenttype): Dokument {
        return Dokument(
            dokument = vedlegg.innhold,
            filtype = Filtype.PDFA,
            tittel = vedlegg.type.tittel,
            filnavn = vedlegg.id.toString(),
            dokumenttype = dokumenttypeVedlegg,
        )
    }
}

fun Dokumenttype?.dokumentTittel(): String {
    return when (this) {
        Dokumenttype.BARNETILSYN_SØKNAD -> "Søknad om barnetilsyn"
        else -> error("Mangler mapping av $this")
    }
}
