package no.nav.tilleggsstonader.soknad.arkivering

import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_BARNETILSYN
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKOLEPENGER
import no.nav.familie.ef.mottak.repository.domain.Ettersending
import no.nav.familie.ef.mottak.repository.domain.EttersendingVedlegg
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.repository.domain.Vedlegg
import no.nav.familie.ef.mottak.util.utledDokumenttypeForEttersending
import no.nav.familie.ef.mottak.util.utledDokumenttypeForVedlegg
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
import no.nav.familie.kontrakter.felles.dokarkiv.v2.ArkiverDokumentRequest
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Dokument
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Filtype
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.tilleggsstonader.kontrakter.dokarkiv.ArkiverDokumentRequest
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokument
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokumenttype
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Filtype
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.soknad.soknad.Søknad

object ArkiverDokumentRequestMapper {

    fun toDto(
        søknad: Søknad,
        vedlegg: List<Vedlegg>,
    ): ArkiverDokumentRequest {
        val dokumenttype = mapDokumenttype(søknad.type)
        val søknadsdokumentJson =
            Dokument(søknad.søknadJson.json.toByteArray(), Filtype.JSON, null, dokumenttype.dokumentTittel(), dokumenttype)
        val søknadsdokumentPdf =
            Dokument(søknad.søknadPdf!!.bytes, Filtype.PDFA, null, dokumenttype.dokumentTittel(), dokumenttype)
        val hoveddokumentvarianter = listOf(søknadsdokumentPdf, søknadsdokumentJson)
        return ArkiverDokumentRequest(
            søknad.fnr,
            false,
            hoveddokumentvarianter,
            mapVedlegg(vedlegg, søknad.dokumenttype),
        )
    }

    private fun mapDokumenttype(type: Stønadstype): Dokumenttype = when(type) {
        Stønadstype.BARNETILSYN -> Dokumenttype.BARNETILSYN_SØKNAD
    }

    fun fromEttersending(
        ettersending: Ettersending,
        vedlegg: List<EttersendingVedlegg>,
    ): ArkiverDokumentRequest {
        val stønadType = StønadType.valueOf(ettersending.stønadType)

        val hovedDokumentVarianter = lagHoveddokumentvarianterForEttersending(stønadType, ettersending)

        return ArkiverDokumentRequest(
            ettersending.fnr,
            false,
            hovedDokumentVarianter,
            mapEttersendingVedlegg(
                vedlegg,
                stønadType,
            ),
        )
    }

    private fun lagHoveddokumentvarianterForEttersending(
        stønadType: StønadType,
        ettersending: Ettersending,
    ): List<Dokument> {
        val tittel = "Ettersending til søknad om $stønadType"
        val dokumenttype = utledDokumenttypeForEttersending(stønadType)

        val dokumentSomPdf = ettersending.ettersendingPdf?.let {
            Dokument(it.bytes, Filtype.PDFA, null, tittel, dokumenttype)
        } ?: error("Mangler forside for ettersendingen")

        val dokumentSomJson = Dokument(ettersending.ettersendingJson.data.toByteArray(), Filtype.JSON, null, tittel, dokumenttype)

        return listOf(dokumentSomPdf, dokumentSomJson)
    }

    private fun mapVedlegg(vedlegg: List<Vedlegg>, dokumenttype: String): List<Dokument> {
        if (vedlegg.isEmpty()) return emptyList()
        val dokumenttypeVedlegg = mapDokumenttype(dokumenttype)
        return vedlegg.map { tilDokument(it, dokumenttypeVedlegg) }
    }

    private fun mapEttersendingVedlegg(vedlegg: List<EttersendingVedlegg>, stønadType: StønadType): List<Dokument> {
        if (vedlegg.isEmpty()) return emptyList()
        val dokumenttypeVedlegg = utledDokumenttypeForVedlegg(stønadType)
        return vedlegg.map { tilEttersendingDokument(it, dokumenttypeVedlegg) }
    }

    private fun tilDokument(vedlegg: Vedlegg, dokumenttypeVedlegg: Dokumenttype): Dokument {
        return Dokument(
            dokument = vedlegg.innhold.bytes,
            filtype = Filtype.PDFA,
            tittel = vedlegg.tittel,
            filnavn = vedlegg.id.toString(),
            dokumenttype = dokumenttypeVedlegg,
        )
    }

    private fun tilEttersendingDokument(vedlegg: EttersendingVedlegg, dokumenttypeVedlegg: Dokumenttype): Dokument {
        return Dokument(
            dokument = vedlegg.innhold.bytes,
            filtype = Filtype.PDFA,
            tittel = vedlegg.tittel,
            filnavn = vedlegg.id.toString(),
            dokumenttype = dokumenttypeVedlegg,
        )
    }

    private fun mapDokumenttype(dokumenttype: String): Dokumenttype {
        return when (dokumenttype) {
            DOKUMENTTYPE_OVERGANGSSTØNAD -> Dokumenttype.OVERGANGSSTØNAD_SØKNAD_VEDLEGG
            DOKUMENTTYPE_BARNETILSYN -> Dokumenttype.BARNETILSYNSTØNAD_VEDLEGG
            DOKUMENTTYPE_SKOLEPENGER -> Dokumenttype.SKOLEPENGER_VEDLEGG
            else -> error("Ukjent dokumenttype=$dokumenttype for vedlegg")
        }
    }
}

fun Dokumenttype?.dokumentTittel(): String {
    return when (this) {
        Dokumenttype.OVERGANGSSTØNAD_SØKNAD -> "Søknad om overgangsstønad"
        Dokumenttype.OVERGANGSSTØNAD_ETTERSENDING -> "Ettersendelse til søknad om overgangsstønad"
        Dokumenttype.SKOLEPENGER_SØKNAD -> "Søknad om stønad til skolepenger"
        Dokumenttype.SKOLEPENGER_ETTERSENDING -> "Ettersendelse til søknad om skolepenger"
        Dokumenttype.BARNETILSYNSTØNAD_SØKNAD -> "Søknad om stønad til barnetilsyn"
        Dokumenttype.BARNETILSYNSTØNAD_ETTERSENDING -> "Ettersendelse til søknad om barnetilsyn"
        else -> "hoveddokument"
    }
}
