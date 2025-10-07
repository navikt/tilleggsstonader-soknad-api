package no.nav.tilleggsstonader.soknad.arkivering

import no.nav.tilleggsstonader.kontrakter.dokarkiv.ArkiverDokumentRequest
import no.nav.tilleggsstonader.kontrakter.dokarkiv.AvsenderMottaker
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokument
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokumenttype
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Filtype
import no.nav.tilleggsstonader.kontrakter.dokarkiv.dokumenttyper
import no.nav.tilleggsstonader.kontrakter.felles.BrukerIdType
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.kontrakter.felles.gjelderDagligReise
import no.nav.tilleggsstonader.soknad.soknad.domene.Skjema
import no.nav.tilleggsstonader.soknad.soknad.domene.Vedlegg

object ArkiverDokumentRequestMapper {
    fun toDto(
        skjema: Skjema,
        vedlegg: List<Vedlegg>,
    ): ArkiverDokumentRequest {
        val dokumenttype = typeHoveddokument(skjema.type)
        val skjemadokumentJson =
            Dokument(
                skjema.skjemaJson.json.toByteArray(),
                Filtype.JSON,
                null,
                dokumenttype.dokumentTittel(),
                dokumenttype,
            )
        val skjemadokumentPdf =
            Dokument(skjema.skjemaPdf!!, Filtype.PDFA, null, dokumenttype.dokumentTittel(), dokumenttype)
        return ArkiverDokumentRequest(
            fnr = skjema.personIdent,
            forsøkFerdigstill = false,
            hoveddokumentvarianter = listOf(skjemadokumentPdf, skjemadokumentJson),
            vedleggsdokumenter = mapVedlegg(vedlegg, skjema.type),
            eksternReferanseId = skjema.id.toString(),
            avsenderMottaker = AvsenderMottaker(id = skjema.personIdent, idType = BrukerIdType.FNR, navn = null),
        )
    }

    // TODO - introduser type skjema, som velger om kjøreliste eller søknad
    private fun typeHoveddokument(type: Stønadstype): Dokumenttype =
        if (type.gjelderDagligReise()) {
            type.dokumenttyper.kjøreliste ?: error("Har ikke laget kjøreliste for $type")
        } else {
            type.dokumenttyper.søknad ?: error("Har ikke laget søknad for $type")
        }

    private fun typeVedlegg(type: Stønadstype): Dokumenttype =
        if (type.gjelderDagligReise()) {
            type.dokumenttyper.kjørelisteVedlegg ?: error("Har ikke laget kjøreliste for $type")
        } else {
            type.dokumenttyper.søknadVedlegg ?: error("Har ikke laget søknad for $type")
        }

    private fun mapVedlegg(
        vedlegg: List<Vedlegg>,
        stønadstype: Stønadstype,
    ): List<Dokument> {
        if (vedlegg.isEmpty()) return emptyList()
        val dokumenttypeVedlegg = typeVedlegg(stønadstype)
        return vedlegg.map { tilDokument(it, dokumenttypeVedlegg) }
    }

    private fun tilDokument(
        vedlegg: Vedlegg,
        dokumenttypeVedlegg: Dokumenttype,
    ): Dokument =
        Dokument(
            dokument = vedlegg.innhold,
            filtype = Filtype.PDFA,
            tittel = vedlegg.type.tittel,
            filnavn = vedlegg.id.toString(),
            dokumenttype = dokumenttypeVedlegg,
        )
}

fun Dokumenttype?.dokumentTittel(): String =
    when (this) {
        Dokumenttype.BARNETILSYN_SØKNAD -> "Søknad om støtte til pass av barn"
        Dokumenttype.LÆREMIDLER_SØKNAD -> "Søknad om støtte til læremidler"
        Dokumenttype.DAGLIG_REISE_TSO_KJØRELISTE, Dokumenttype.DAGLIG_REISE_TSR_KJØRELISTE,
        -> "Refusjon av utgifter til daglig reise med bruk av egen bil"
        else -> error("Mangler mapping av $this")
    }
