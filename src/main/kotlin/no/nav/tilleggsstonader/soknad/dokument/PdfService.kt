package no.nav.tilleggsstonader.soknad.dokument

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.tilleggsstonader.kontrakter.felles.ObjectMapperProvider.objectMapper
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.kontrakter.søknad.Søknadsskjema
import no.nav.tilleggsstonader.kontrakter.søknad.SøknadsskjemaBarnetilsyn
import no.nav.tilleggsstonader.kontrakter.søknad.SøknadsskjemaLæremidler
import no.nav.tilleggsstonader.soknad.dokument.pdf.SpråkMapper.tittelSøknadsskjema
import no.nav.tilleggsstonader.soknad.dokument.pdf.Søkerinformasjon
import no.nav.tilleggsstonader.soknad.dokument.pdf.SøknadTreeWalker.mapSøknad
import no.nav.tilleggsstonader.soknad.dokument.pdf.VedleggMapper.mapVedlegg
import no.nav.tilleggsstonader.soknad.person.PersonService
import no.nav.tilleggsstonader.soknad.soknad.SøknadService
import no.nav.tilleggsstonader.soknad.soknad.domene.Søknad
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PdfService(
    private val søknadService: SøknadService,
    private val personService: PersonService,
    private val htmlifyClient: HtmlifyClient,
    private val dokumentClient: FamilieDokumentClient,
) {
    fun lagPdf(søknadId: UUID) {
        val søknad = søknadService.hentSøknad(søknadId)
        val søknadsskjema = parseSøknadsskjema(søknad)
        val felter = mapSøknad(søknadsskjema, hentSøkerinformasjon(søknad))
        val html =
            htmlifyClient.generateHtml(
                stønadstype = søknad.type,
                tittel = tittelSøknadsskjema(søknadsskjema),
                felter = felter,
                mottattTidspunkt = søknadsskjema.mottattTidspunkt,
                dokumentasjon = mapVedlegg(søknadsskjema),
            )
        val pdf = dokumentClient.genererPdf(html)
        søknadService.oppdaterSøknad(søknad.copy(søknadPdf = pdf))
    }

    private fun hentSøkerinformasjon(søknad: Søknad): Søkerinformasjon {
        val navn = personService.hentNavnMedClientCredential(søknad.personIdent)
        return Søkerinformasjon(ident = søknad.personIdent, navn = navn)
    }

    private fun parseSøknadsskjema(søknad: Søknad): Søknadsskjema<*> {
        val json = søknad.søknadJson.json
        return when (søknad.type) {
            Stønadstype.BARNETILSYN -> objectMapper.readValue<Søknadsskjema<SøknadsskjemaBarnetilsyn>>(json)
            Stønadstype.LÆREMIDLER -> objectMapper.readValue<Søknadsskjema<SøknadsskjemaLæremidler>>(json)
        }
    }
}
