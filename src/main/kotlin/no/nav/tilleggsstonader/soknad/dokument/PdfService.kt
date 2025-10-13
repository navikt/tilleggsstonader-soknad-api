package no.nav.tilleggsstonader.soknad.dokument

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.tilleggsstonader.kontrakter.felles.ObjectMapperProvider.objectMapper
import no.nav.tilleggsstonader.kontrakter.felles.Skjematype
import no.nav.tilleggsstonader.kontrakter.felles.tilStønadstyper
import no.nav.tilleggsstonader.kontrakter.sak.DokumentBrevkode
import no.nav.tilleggsstonader.kontrakter.søknad.InnsendtSkjema
import no.nav.tilleggsstonader.kontrakter.søknad.KjørelisteSkjema
import no.nav.tilleggsstonader.kontrakter.søknad.SøknadsskjemaBarnetilsyn
import no.nav.tilleggsstonader.kontrakter.søknad.SøknadsskjemaLæremidler
import no.nav.tilleggsstonader.soknad.dokument.pdf.SpråkMapper.tittelSøknadsskjema
import no.nav.tilleggsstonader.soknad.dokument.pdf.Søkerinformasjon
import no.nav.tilleggsstonader.soknad.dokument.pdf.SøknadTreeWalker.mapSøknad
import no.nav.tilleggsstonader.soknad.dokument.pdf.VedleggMapper.mapVedlegg
import no.nav.tilleggsstonader.soknad.infrastruktur.database.ByteArrayWrapper
import no.nav.tilleggsstonader.soknad.person.PersonService
import no.nav.tilleggsstonader.soknad.soknad.SkjemaService
import no.nav.tilleggsstonader.soknad.soknad.domene.Skjema
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PdfService(
    private val skjemaService: SkjemaService,
    private val personService: PersonService,
    private val htmlifyClient: HtmlifyClient,
    private val dokumentClient: FamilieDokumentClient,
) {
    fun lagPdf(skjemaId: UUID) {
        val skjema = skjemaService.hentSkjema(skjemaId)
        val innsendtSkjema = parseInnsendtSkjema(skjema)

        val html =
            when (innsendtSkjema.skjema) {
                is SøknadsskjemaBarnetilsyn, is SøknadsskjemaLæremidler -> genererSøknadHtml(skjema, innsendtSkjema)
                is KjørelisteSkjema ->
                    genererKjørelisteHtml(
                        skjema,
                        innsendtSkjema,
                        innsendtSkjema.skjema as KjørelisteSkjema,
                    )
                else -> error("Støtter ikke pdf-generering for skjema av type ${skjema.type}")
            }

        val pdf = dokumentClient.genererPdf(html)
        skjemaService.oppdaterSkjema(skjema.copy(skjemaPdf = ByteArrayWrapper(pdf)))
    }

    private fun genererSøknadHtml(
        skjema: Skjema,
        innsendtSkjema: InnsendtSkjema<*>,
    ): String {
        val felter = mapSøknad(innsendtSkjema, hentSøkerinformasjon(skjema))
        return htmlifyClient.genererSøknadHtml(
            // TODO - stønadstype kan nok fjernes
            stønadstype = skjema.type.tilStønadstyper().first(),
            tittel = tittelSøknadsskjema(innsendtSkjema),
            felter = felter,
            mottattTidspunkt = innsendtSkjema.mottattTidspunkt,
            dokumentasjon = mapVedlegg(innsendtSkjema),
            dokumentBrevkode = dokumentBrevKode(innsendtSkjema),
        )
    }

    private fun genererKjørelisteHtml(
        skjema: Skjema,
        innsendtSkjema: InnsendtSkjema<*>,
        kjørelisteSkjema: KjørelisteSkjema,
    ) = htmlifyClient.genererKjørelisteHtml(
        lagKjørelisteHtmlRequest(skjema, innsendtSkjema, kjørelisteSkjema),
    )

    private fun lagKjørelisteHtmlRequest(
        skjema: Skjema,
        innsendtSkjema: InnsendtSkjema<*>,
        kjørelisteSkjema: KjørelisteSkjema,
    ): KjørelisteHtmlRequest =
        KjørelisteHtmlRequest(
            tittel = tittelSøknadsskjema(innsendtSkjema),
            skjemanummer = dokumentBrevKode(innsendtSkjema).verdi,
            mottattTidspunkt = innsendtSkjema.mottattTidspunkt,
            uker =
                kjørelisteSkjema.reisedagerPerUkeAvsnitt.map { uke ->
                    KjørelisteUkeHtmlRequest(
                        ukeLabel = uke.ukeLabel,
                        spørsmål = uke.spørsmål,
                        dager =
                            uke.reisedager.map { dag ->
                                KjørelisteDagHtmlRequest(
                                    datoLabel = dag.dato.label,
                                    harKjørt = dag.harKjørt,
                                    parkeringsutgift =
                                        dag.parkeringsutgift?.let { parkeringsutgift ->
                                            KjørelisteParkeringsutgiftHtmlRequest(
                                                label = parkeringsutgift.label,
                                                beløp = parkeringsutgift.verdi,
                                            )
                                        },
                                )
                            },
                    )
                },
            dokumentasjon = mapVedlegg(innsendtSkjema),
            søker = hentSøkerinformasjon(skjema),
        )

    private fun dokumentBrevKode(innsendtSkjema: InnsendtSkjema<*>): DokumentBrevkode =
        when (innsendtSkjema.skjema) {
            is SøknadsskjemaBarnetilsyn -> DokumentBrevkode.BARNETILSYN
            is SøknadsskjemaLæremidler -> DokumentBrevkode.LÆREMIDLER
            is KjørelisteSkjema -> DokumentBrevkode.DAGLIG_REISE_KJØRELISTE
            else -> error("Ingen dokumentbrevkode for skjema ${innsendtSkjema.skjema::class.qualifiedName}")
        }

    private fun hentSøkerinformasjon(skjema: Skjema): Søkerinformasjon {
        val navn = personService.hentNavnMedClientCredential(skjema.personIdent)
        return Søkerinformasjon(ident = skjema.personIdent, navn = navn)
    }

    private fun parseInnsendtSkjema(skjema: Skjema): InnsendtSkjema<*> {
        val json = skjema.skjemaJson.json
        return when (skjema.type) {
            Skjematype.SØKNAD_BARNETILSYN -> objectMapper.readValue<InnsendtSkjema<SøknadsskjemaBarnetilsyn>>(json)
            Skjematype.SØKNAD_LÆREMIDLER -> objectMapper.readValue<InnsendtSkjema<SøknadsskjemaLæremidler>>(json)
            Skjematype.DAGLIG_REISE_KJØRELISTE -> objectMapper.readValue<InnsendtSkjema<KjørelisteSkjema>>(json)
            Skjematype.SØKNAD_BOUTGIFTER, Skjematype.SØKNAD_DAGLIG_REISE ->
                error("Håndterer ikke skjema ${skjema.type}")
        }
    }
}
