package no.nav.tilleggsstonader.soknad.arkivering

import no.nav.tilleggsstonader.kontrakter.dokarkiv.ArkiverDokumentRequest
import no.nav.tilleggsstonader.kontrakter.dokarkiv.AvsenderMottaker
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokument
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokumenttype
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Filtype
import no.nav.tilleggsstonader.kontrakter.felles.BrukerIdType
import no.nav.tilleggsstonader.kontrakter.felles.Skjematype
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.soknad.soknad.domene.Skjema
import no.nav.tilleggsstonader.soknad.soknad.domene.Vedlegg

object ArkiverDokumentRequestMapper {
    fun toDto(
        skjema: Skjema,
        tilhørendeStønadstype: Stønadstype,
        vedlegg: List<Vedlegg>,
    ): ArkiverDokumentRequest {
        val dokumenttype = typeHoveddokument(skjema.type, tilhørendeStønadstype)
        val skjemadokumentJson =
            Dokument(
                skjema.skjemaJson.json.toByteArray(),
                Filtype.JSON,
                null,
                dokumenttype.dokumentTittel(),
                dokumenttype,
            )
        val skjemadokumentPdf =
            Dokument(skjema.skjemaPdf!!.data, Filtype.PDFA, null, dokumenttype.dokumentTittel(), dokumenttype)
        return ArkiverDokumentRequest(
            fnr = skjema.personIdent,
            forsøkFerdigstill = false,
            hoveddokumentvarianter = listOf(skjemadokumentPdf, skjemadokumentJson),
            vedleggsdokumenter = mapVedlegg(vedlegg, skjema.type, tilhørendeStønadstype),
            eksternReferanseId = skjema.id.toString(),
            avsenderMottaker = AvsenderMottaker(id = skjema.personIdent, idType = BrukerIdType.FNR, navn = null),
        )
    }

    private fun typeHoveddokument(
        type: Skjematype,
        tilhørendeStønadstype: Stønadstype,
    ): Dokumenttype =
        when (type) {
            Skjematype.SØKNAD_BARNETILSYN -> Dokumenttype.BARNETILSYN_SØKNAD
            Skjematype.SØKNAD_LÆREMIDLER -> Dokumenttype.LÆREMIDLER_SØKNAD
            Skjematype.DAGLIG_REISE_KJØRELISTE ->
                if (tilhørendeStønadstype == Stønadstype.DAGLIG_REISE_TSO) {
                    Dokumenttype.DAGLIG_REISE_TSO_KJØRELISTE
                } else {
                    Dokumenttype.DAGLIG_REISE_TSR_KJØRELISTE
                }
            Skjematype.SØKNAD_BOUTGIFTER, Skjematype.SØKNAD_DAGLIG_REISE ->
                error("Håndterer ikke skjema $type")
        }

    private fun typeVedlegg(
        type: Skjematype,
        tilhørendeStønadstype: Stønadstype,
    ): Dokumenttype =
        when (type) {
            Skjematype.SØKNAD_BARNETILSYN -> Dokumenttype.BARNETILSYN_SØKNAD_VEDLEGG
            Skjematype.SØKNAD_LÆREMIDLER -> Dokumenttype.LÆREMIDLER_SØKNAD_VEDLEGG
            Skjematype.DAGLIG_REISE_KJØRELISTE ->
                if (tilhørendeStønadstype == Stønadstype.DAGLIG_REISE_TSO) {
                    Dokumenttype.DAGLIG_REISE_TSO_KJØRELISTE_VEDLEGG
                } else {
                    Dokumenttype.DAGLIG_REISE_TSR_KJØRELISTE_VEDLEGG
                }
            Skjematype.SØKNAD_BOUTGIFTER, Skjematype.SØKNAD_DAGLIG_REISE ->
                error("Håndterer ikke skjema $type")
        }

    private fun mapVedlegg(
        vedlegg: List<Vedlegg>,
        skjematype: Skjematype,
        tilhørendeStønadstype: Stønadstype,
    ): List<Dokument> {
        if (vedlegg.isEmpty()) return emptyList()
        val dokumenttypeVedlegg = typeVedlegg(skjematype, tilhørendeStønadstype)
        return vedlegg.map { tilDokument(it, dokumenttypeVedlegg) }
    }

    private fun tilDokument(
        vedlegg: Vedlegg,
        dokumenttypeVedlegg: Dokumenttype,
    ): Dokument =
        Dokument(
            dokument = vedlegg.innhold.data,
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
