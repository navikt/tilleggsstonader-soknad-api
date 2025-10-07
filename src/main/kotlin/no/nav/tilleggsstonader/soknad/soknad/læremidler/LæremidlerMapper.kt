package no.nav.tilleggsstonader.soknad.soknad.læremidler

import no.nav.tilleggsstonader.kontrakter.felles.Språkkode
import no.nav.tilleggsstonader.kontrakter.søknad.InnsendtSkjema
import no.nav.tilleggsstonader.kontrakter.søknad.SøknadsskjemaLæremidler
import no.nav.tilleggsstonader.kontrakter.søknad.læremidler.HarRettTilUtstyrsstipend
import no.nav.tilleggsstonader.kontrakter.søknad.læremidler.UtdanningAvsnitt
import no.nav.tilleggsstonader.soknad.soknad.SøknadMapper
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class LæremidlerMapper {
    fun map(
        ident: String,
        mottattTidspunkt: LocalDateTime,
        dto: SøknadLæremidlerDto,
    ): InnsendtSkjema<SøknadsskjemaLæremidler> {
        val språkkode = Språkkode.NB
        return InnsendtSkjema(
            ident = ident,
            mottattTidspunkt = mottattTidspunkt,
            språk = språkkode,
            skjema =
                SøknadsskjemaLæremidler(
                    hovedytelse = SøknadMapper.mapHovedytelse(dto.hovedytelse),
                    utdanning = mapUtdanning(dto),
                    dokumentasjon = dto.dokumentasjon,
                ),
        )
    }

    private fun mapUtdanning(dto: SøknadLæremidlerDto) =
        UtdanningAvsnitt(
            aktiviteter = dto.utdanning.aktiviteter,
            annenUtdanning = dto.utdanning.annenUtdanning,
            harRettTilUtstyrsstipend =
                HarRettTilUtstyrsstipend(
                    erLærlingEllerLiknende = dto.utdanning.harRettTilUtstyrsstipend?.erLærlingEllerLiknende,
                    harTidligereFullførtVgs = dto.utdanning.harRettTilUtstyrsstipend?.harTidligereFullførtVgs,
                ),
            harFunksjonsnedsettelse = dto.utdanning.harFunksjonsnedsettelse,
        )
}
