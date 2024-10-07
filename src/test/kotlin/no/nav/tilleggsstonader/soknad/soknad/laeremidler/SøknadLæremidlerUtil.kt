package no.nav.tilleggsstonader.soknad.soknad.laeremidler

import no.nav.tilleggsstonader.kontrakter.søknad.EnumFelt
import no.nav.tilleggsstonader.kontrakter.søknad.JaNei
import no.nav.tilleggsstonader.kontrakter.søknad.læremidler.AnnenUtdanningType
import no.nav.tilleggsstonader.soknad.soknad.SøknadTestUtil

object SøknadLæremidlerUtil {
    val søknad = SøknadLæremidlerDto(
        hovedytelse = SøknadTestUtil.hovedytelseDto(),
        utdanning = Utdanning(
            annenUtdanning = EnumFelt(
                label = "Hva slags utdanning eller opplæring skal du ta?",
                verdi = AnnenUtdanningType.FAGSKOLE_HØGSKOLE_UNIVERSITET,
                svarTekst = "Utdanning på fagskole, høgskole eller universitet",
                alternativer = listOf(),
            ),
            mottarUtstyrsstipend = EnumFelt(
                label = "Mottar du utstyrsstipend fra Statens lånekasse?",
                verdi = JaNei.JA,
                svarTekst = "Ja",
                alternativer = listOf(),
            ),
            harFunksjonsnedsettelse = EnumFelt(
                label = "Har du en funksjonsnedsettelse som gir deg særlig store utgifter til læremidler?",
                verdi = JaNei.JA,
                svarTekst = "Ja",
                alternativer = listOf(),
            ),
        ),
        dokumentasjon = listOf(),
    )
}
