package no.nav.tilleggsstonader.soknad.arkivering

import no.nav.tilleggsstonader.kontrakter.dokarkiv.ArkiverDokumentRequest
import no.nav.tilleggsstonader.kontrakter.dokarkiv.AvsenderMottaker
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokument
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokumenttype
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Filtype
import no.nav.tilleggsstonader.kontrakter.dokarkiv.dokumenttyper
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
        val dokumenttype = typeHoveddokument(type = skjema.type, tilhørendeStønadstype = tilhørendeStønadstype)
        val skjemadokumentJson =
            Dokument(
                dokument = skjema.skjemaJson.json.toByteArray(),
                filtype = Filtype.JSON,
                filnavn = null,
                tittel = dokumenttype.dokumentTittel(),
                dokumenttype = dokumenttype,
            )
        val skjemadokumentPdf =
            Dokument(
                dokument = skjema.skjemaPdf!!.data,
                filtype = Filtype.PDFA,
                filnavn = null,
                tittel = dokumenttype.dokumentTittel(),
                dokumenttype = dokumenttype,
            )
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

            Skjematype.SØKNAD_REISE_TIL_SAMLING -> Dokumenttype.REISE_TIL_SAMLING_TSO_SØKNAD

            // Stønadstype blir alltid nå satt til TSO, så setter bare denne direkte
            Skjematype.SØKNAD_FLYTTING -> Dokumenttype.FLYTTING_TSO_SØKNAD

            Skjematype.SØKNAD_REISE_OPPSTART_AVSLUTNING_HJEMREISE ->
                tilhørendeStønadstype.dokumenttyper.søknad
                    ?: error("Finner ikke dokumenttype for $type og stønadstype=$tilhørendeStønadstype")
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

            Skjematype.SØKNAD_REISE_TIL_SAMLING -> Dokumenttype.REISE_TIL_SAMLING_TSO_SØKNAD_VEDLEGG

            // Stønadstype blir alltid nå satt til TSO, så setter bare denne direkte
            Skjematype.SØKNAD_FLYTTING -> Dokumenttype.FLYTTING_TSO_SØKNAD_VEDLEGG

            Skjematype.SØKNAD_REISE_OPPSTART_AVSLUTNING_HJEMREISE ->
                tilhørendeStønadstype.dokumenttyper.søknadVedlegg
                    ?: error("Finner ikke dokumenttype for vedlegg for $type og stønadstype=$tilhørendeStønadstype")
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
        Dokumenttype.REISE_TIL_SAMLING_TSO_SØKNAD, Dokumenttype.REISE_TIL_SAMLING_TSR_SØKNAD -> "Søknad om støtte til reise til samling"
        Dokumenttype.FLYTTING_TSO_SØKNAD, Dokumenttype.FLYTTING_TSR_SØKNAD -> "Søknad om støtte til flytting"
        Dokumenttype.REISE_OPPSTART_AVSLUTNING_HJEMREISE_TSO_SØKNAD,
        Dokumenttype.REISE_OPPSTART_AVSLUTNING_HJEMREISE_TSR_SØKNAD,
        -> "Søknad om støtte til reise til oppstart, avslutning og hjemreiser"
        Dokumenttype.DAGLIG_REISE_TSO_KJØRELISTE, Dokumenttype.DAGLIG_REISE_TSR_KJØRELISTE,
        -> "Refusjon av utgifter til daglig reise med bruk av bil"

        else -> error("Mangler mapping av $this")
    }
