package no.nav.tilleggsstonader.soknad.dokument

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.tilleggsstonader.kontrakter.felles.ObjectMapperProvider.objectMapper
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.kontrakter.sak.DokumentBrevkode
import no.nav.tilleggsstonader.kontrakter.søknad.KjørelisteSkjema
import no.nav.tilleggsstonader.kontrakter.søknad.Søknadsskjema
import no.nav.tilleggsstonader.kontrakter.søknad.SøknadsskjemaBarnetilsyn
import no.nav.tilleggsstonader.kontrakter.søknad.SøknadsskjemaLæremidler
import no.nav.tilleggsstonader.soknad.dokument.pdf.SpråkMapper.tittelSøknadsskjema
import no.nav.tilleggsstonader.soknad.dokument.pdf.Søkerinformasjon
import no.nav.tilleggsstonader.soknad.dokument.pdf.SøknadTreeWalker.mapSøknad
import no.nav.tilleggsstonader.soknad.dokument.pdf.VedleggMapper.mapVedlegg
import no.nav.tilleggsstonader.soknad.person.PersonService
import no.nav.tilleggsstonader.soknad.soknad.SøknadService
import no.nav.tilleggsstonader.soknad.soknad.domene.Skjema
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
                dokumentBrevkode = dokumentBrevKode(søknadsskjema),
            )
        val pdf = dokumentClient.genererPdf(html)
        søknadService.oppdaterSøknad(søknad.copy(søknadPdf = pdf))
    }

    // TODO - mappe dokumentbrevkode fra skjematype
    private fun dokumentBrevKode(søknadsskjema: Søknadsskjema<*>): DokumentBrevkode =
        when (søknadsskjema.skjema) {
            is SøknadsskjemaBarnetilsyn -> DokumentBrevkode.BARNETILSYN
            is SøknadsskjemaLæremidler -> DokumentBrevkode.LÆREMIDLER
            is KjørelisteSkjema -> DokumentBrevkode.DAGLIG_REISE_KJØRELISTE
            else -> error("Ingen dokumentbrevkode for skjema ${søknadsskjema.skjema::class.qualifiedName}")
        }

    private fun hentSøkerinformasjon(skjema: Skjema): Søkerinformasjon {
        val navn = personService.hentNavnMedClientCredential(skjema.personIdent)
        return Søkerinformasjon(ident = skjema.personIdent, navn = navn)
    }

    private fun parseSøknadsskjema(skjema: Skjema): Søknadsskjema<*> {
        val json = skjema.søknadJson.json
        return when (skjema.type) {
            Stønadstype.BARNETILSYN -> objectMapper.readValue<Søknadsskjema<SøknadsskjemaBarnetilsyn>>(json)
            Stønadstype.LÆREMIDLER -> objectMapper.readValue<Søknadsskjema<SøknadsskjemaLæremidler>>(json)
            Stønadstype.DAGLIG_REISE_TSO, Stønadstype.DAGLIG_REISE_TSR -> objectMapper.readValue<Søknadsskjema<KjørelisteSkjema>>(json)
            Stønadstype.BOUTGIFTER ->
                error("Har ikke laget søknad for ${skjema.type}")
        }
    }
}
