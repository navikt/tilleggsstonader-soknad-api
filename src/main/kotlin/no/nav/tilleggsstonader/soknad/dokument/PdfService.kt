package no.nav.tilleggsstonader.soknad.dokument

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.tilleggsstonader.kontrakter.felles.ObjectMapperProvider.objectMapper
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.kontrakter.søknad.Søknadsskjema
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.SøknadsskjemaBarnetilsyn
import no.nav.tilleggsstonader.soknad.dokument.pdf.SøknadTreeWalker.mapSøknad
import no.nav.tilleggsstonader.soknad.soknad.SøknadService
import no.nav.tilleggsstonader.soknad.soknad.domene.Søknad
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PdfService(
    private val søknadService: SøknadService,
    private val htmlifyClient: HtmlifyClient,
    private val dokumentClient: FamilieDokumentClient,
) {

    fun lagPdf(søknadId: UUID) {
        val søknad = søknadService.hentSøknad(søknadId)
        val vedleggtitler = søknadService.finnVedleggTitlerForSøknad(søknadId)
        val søknadsskjema = parseSøknadsskjema(søknad)
        val feltMap = mapSøknad(søknadsskjema, vedleggtitler)
        val html = htmlifyClient.generateHtml(søknad.type, feltMap, søknadsskjema.mottattTidspunkt)
        val pdf = dokumentClient.genererPdf(html)
        søknadService.oppdaterSøknad(søknad.copy(søknadPdf = pdf))
    }

    private fun parseSøknadsskjema(
        søknad: Søknad,
    ): Søknadsskjema<*> {
        val json = søknad.søknadJson.json
        return when (søknad.type) {
            Stønadstype.BARNETILSYN -> objectMapper.readValue<Søknadsskjema<SøknadsskjemaBarnetilsyn>>(json)
        }
    }
}
