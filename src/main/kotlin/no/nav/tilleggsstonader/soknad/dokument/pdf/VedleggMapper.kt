package no.nav.tilleggsstonader.soknad.dokument.pdf

import no.nav.tilleggsstonader.kontrakter.felles.Språkkode
import no.nav.tilleggsstonader.kontrakter.søknad.DokumentasjonFelt
import no.nav.tilleggsstonader.kontrakter.søknad.Søknadsskjema
import no.nav.tilleggsstonader.kontrakter.søknad.SøknadsskjemaBarnetilsyn
import no.nav.tilleggsstonader.soknad.dokument.Dokument
import no.nav.tilleggsstonader.soknad.dokument.DokumentasjonAvsnitt

object VedleggMapper {
    fun mapVedlegg(søknad: Søknadsskjema<*>): List<DokumentasjonAvsnitt> {
        require(søknad.språk == Språkkode.NB) {
            "Må legge inn mapping av språk ${søknad.språk} for vedlegg: tittel, har sendt inn tidligere "
        }
        val identTilNavn = mapBarnIdentTilNavn(søknad)
        val dokumentasjon = søknad.skjema.dokumentasjon
        return dokumentasjon.groupBy { it.barnId }.map { (barnId, values) ->
            val verdier = mapVedlegg(values.sortedBy { it.type })
            val navn = navnBarn(barnId, identTilNavn) ?: ""
            DokumentasjonAvsnitt("Vedlegg $navn", verdier)
        }
    }

    private fun navnBarn(
        barnId: String?,
        identTilNavn: Map<String, String>,
    ): String? =
        barnId?.let {
            identTilNavn[it] ?: error("Finner ikke barn=$barnId")
        }

    private fun mapVedlegg(dokumentasjon: List<DokumentasjonFelt>): List<Dokument> =
        dokumentasjon.map {
            Dokument(
                label = it.type.tittel,
                labelAntall = "Antall: ${it.opplastedeVedlegg.count()}",
            )
        }

    private fun mapBarnIdentTilNavn(søknad: Søknadsskjema<*>): Map<String, String> {
        val skjema = søknad.skjema
        return if (skjema is SøknadsskjemaBarnetilsyn) {
            skjema.barn.barnMedBarnepass.associate { it.ident.verdi to it.navn.verdi }
        } else {
            emptyMap()
        }
    }
}
