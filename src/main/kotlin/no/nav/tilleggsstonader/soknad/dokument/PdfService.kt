package no.nav.tilleggsstonader.soknad.dokument

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.tilleggsstonader.kontrakter.felles.ObjectMapperProvider.objectMapper
import no.nav.tilleggsstonader.kontrakter.felles.Språkkode
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.soknad.dokument.pdf.HtmlGenerator
import no.nav.tilleggsstonader.soknad.dokument.pdf.SøknadTreeWalker.mapSøknad
import no.nav.tilleggsstonader.soknad.soknad.SøknadService
import no.nav.tilleggsstonader.soknad.soknad.domene.Søknad
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PdfService(
    private val søknadService: SøknadService,
    private val htmlGenerator: HtmlGenerator,
    private val dokumentClient: FamilieDokumentClient,
) {

    // todo søknad som lagres ned burde være Søknadsskjema
    fun lagPdf(søknadId: UUID) {
        val søknad = søknadService.hentSøknad(søknadId)
        val vedleggtitler = listOf<String>() // TODO
        val feltMap = lagFeltMap(søknad, vedleggtitler, Språkkode.NB) // TODO språk
        val html = htmlGenerator.generateHtml(søknad.type, feltMap)
        val pdf = dokumentClient.genererPdf(html)
        søknadService.oppdaterSøknad(søknad.copy(søknadPdf = pdf))
    }

    private fun lagFeltMap(
        søknad: Søknad,
        vedleggtitler: List<String>,
        språk: Språkkode,
    ) = when (søknad.type) {
        Stønadstype.BARNETILSYN -> mapSøknad(objectMapper.readValue(søknad.søknadJson.json), vedleggtitler, språk)
    }
}
