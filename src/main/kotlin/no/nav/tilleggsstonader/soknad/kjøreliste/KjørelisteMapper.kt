package no.nav.tilleggsstonader.soknad.kjøreliste

import no.nav.tilleggsstonader.kontrakter.felles.Språkkode
import no.nav.tilleggsstonader.kontrakter.søknad.KjørelisteSkjema
import no.nav.tilleggsstonader.kontrakter.søknad.Reisedag
import no.nav.tilleggsstonader.kontrakter.søknad.Søknadsskjema
import no.nav.tilleggsstonader.kontrakter.søknad.UkeMedReisedager
import java.time.LocalDateTime

object KjørelisteMapper {
    fun map(
        ident: String,
        mottattTidspunkt: LocalDateTime,
        dto: KjørelisteDto,
    ): Søknadsskjema<KjørelisteSkjema> {
        val språkkode = Språkkode.NB
        return Søknadsskjema(
            ident = ident,
            mottattTidspunkt = mottattTidspunkt,
            språk = språkkode,
            skjema =
                KjørelisteSkjema(
                    reisedagerPerUkeAvsnitt = dto.reisedagerPerUkeAvsnitt.map { it.mapTilSkjema() },
                    dokumentasjon = dto.dokumentasjon,
                ),
        )
    }
}

private fun UkeMedReisedagerDto.mapTilSkjema(): UkeMedReisedager =
    UkeMedReisedager(
        ukeLabel = ukeLabel,
        reisedager = reisedager.map { Reisedag(it.dato, it.parkeringsutgift) },
    )
