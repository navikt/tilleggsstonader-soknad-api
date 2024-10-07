package no.nav.tilleggsstonader.soknad.soknad.laeremidler

import no.nav.tilleggsstonader.kontrakter.felles.Språkkode
import no.nav.tilleggsstonader.kontrakter.søknad.Søknadsskjema
import no.nav.tilleggsstonader.kontrakter.søknad.SøknadsskjemaLæremidler
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
    ): Søknadsskjema<SøknadsskjemaLæremidler> {
        val språkkode = Språkkode.NB
        return Søknadsskjema(
            ident = ident,
            mottattTidspunkt = mottattTidspunkt,
            språk = språkkode,
            skjema = SøknadsskjemaLæremidler(
                hovedytelse = SøknadMapper.mapHovedytelse(dto.hovedytelse),
                utdanning = mapUtdanning(dto),
                dokumentasjon = dto.dokumentasjon,
            ),
        )
    }

    private fun mapUtdanning(dto: SøknadLæremidlerDto) = UtdanningAvsnitt(
        annenUtdanning = dto.utdanning.annenUtdanning,
        mottarUtstyrsstipend = dto.utdanning.mottarUtstyrsstipend,
        harFunksjonsnedsettelse = dto.utdanning.harFunksjonsnedsettelse,
    )
}
