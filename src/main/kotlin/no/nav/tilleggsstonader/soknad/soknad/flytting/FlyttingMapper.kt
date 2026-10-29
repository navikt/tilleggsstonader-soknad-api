package no.nav.tilleggsstonader.soknad.soknad.flytting

import no.nav.tilleggsstonader.kontrakter.felles.Språkkode
import no.nav.tilleggsstonader.kontrakter.søknad.InnsendtSkjema
import no.nav.tilleggsstonader.kontrakter.søknad.SøknadsskjemaFlytting
import no.nav.tilleggsstonader.kontrakter.søknad.VerdiFelt
import no.nav.tilleggsstonader.kontrakter.søknad.flytting.FlyttebyråAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.flytting.FlyttebyråTilbud
import no.nav.tilleggsstonader.kontrakter.søknad.flytting.Flyttemåte
import no.nav.tilleggsstonader.kontrakter.søknad.flytting.FlyttingDetaljerAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.flytting.FraAdresseAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.flytting.TilAdresseAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.flytting.UtgifterEgenFlyttingAvsnitt
import no.nav.tilleggsstonader.soknad.soknad.SøknadMapper
import no.nav.tilleggsstonader.soknad.soknad.SøknadValideringException
import no.nav.tilleggsstonader.soknad.soknad.reiseTilSamling.AdresseDto
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

@Service
class FlyttingMapper {
    private val beløpsformat = Regex("""\d+(?:[.,]\d{1,2})?""")

    fun map(
        ident: String,
        mottattTidspunkt: LocalDateTime,
        dto: SøknadFlyttingDto,
    ): InnsendtSkjema<SøknadsskjemaFlytting> =
        InnsendtSkjema(
            ident = ident,
            mottattTidspunkt = mottattTidspunkt,
            språk = Språkkode.NB,
            skjema =
                SøknadsskjemaFlytting(
                    hovedytelse = SøknadMapper.mapHovedytelse(dto.hovedytelse),
                    flyttingDetaljer = dto.flyttingDetaljer?.let { mapFlyttingDetaljer(it) },
                    dokumentasjon = dto.dokumentasjon,
                ),
        )

    private fun mapFlyttingDetaljer(dto: FlyttingDetaljerDto) =
        FlyttingDetaljerAvsnitt(
            årsak = dto.årsak,
            flyttedato = dto.flyttedato,
            adresse = mapTilAdresse(dto.adresse),
            flyttemåte = dto.flyttemåte,
            utgifterDekketPåAnnenMåte = dto.utgifterDekketPåAnnenMåte,
            skalFlytteFraFolkeregistrertAdresse = dto.skalFlytteFraFolkeregistrertAdresse,
            adresseDetSkalFlyttesFra = mapFraAdresse(dto.adresseDetSkalFlyttesFra),
            utgifterEgenFlytting =
                if (dto.flyttemåte.verdi == Flyttemåte.FLYTTER_SELV) {
                    mapUtgifterEgenFlytting(dto.utgifterEgenFlytting ?: ugyldigFelt("utgifterEgenFlytting"))
                } else {
                    null
                },
            flyttebyrå =
                if (dto.flyttemåte.verdi == Flyttemåte.FLYTTEBYRÅ) {
                    mapFlyttebyrå(dto.flyttebyrå ?: ugyldigFelt("flyttebyrå"))
                } else {
                    null
                },
        )

    private fun mapTilAdresse(dto: AdresseDto) =
        TilAdresseAvsnitt(
            land = dto.land,
            gateadresse = dto.gateadresse,
            postnummer = dto.postnummer,
            poststed = dto.poststed,
        )

    private fun mapFraAdresse(dto: AdresseDto) =
        FraAdresseAvsnitt(
            land = dto.land,
            gateadresse = dto.gateadresse,
            postnummer = dto.postnummer,
            poststed = dto.poststed,
        )

    private fun mapUtgifterEgenFlytting(dto: UtgifterEgenFlyttingDto): UtgifterEgenFlyttingAvsnitt {
        val avstand = parsePåkrevdBeløp("avstand", dto.avstand)
        if (avstand.verdi <= BigDecimal.ZERO) {
            ugyldigFelt("avstand")
        }
        return UtgifterEgenFlyttingAvsnitt(
            avstand = avstand,
            hengerleie = parseBeløp("hengerleie", dto.hengerleie),
            leiebil = parseBeløp("leiebil", dto.leiebil),
            bom = parseBeløp("bom", dto.bom),
            parkering = parseBeløp("parkering", dto.parkering),
            ferje = parseBeløp("ferje", dto.ferje),
            annet = parseBeløp("annet", dto.annet),
        )
    }

    private fun mapFlyttebyrå(dto: FlyttebyråDto): FlyttebyråAvsnitt {
        val dtoTilbud = dto.tilbud
        if (dtoTilbud.size != ANTALL_TILBUD) {
            ugyldigFelt("tilbud")
        }
        val tilbud =
            dtoTilbud.mapIndexed { index, tilbud ->
                val navn = tilbud.navn.verdi
                if (navn.isBlank() || navn.length > MAKS_NAVNELENGDE) {
                    ugyldigFelt("tilbud[$index].navn")
                }
                mapFlyttebyråTilbud(tilbud)
            }
        return FlyttebyråAvsnitt(
            tilbud = tilbud,
        )
    }

    private fun mapFlyttebyråTilbud(dto: FlyttebyråTilbudDto) =
        FlyttebyråTilbud(
            navn = dto.navn,
            beløp = parsePåkrevdBeløp("beløp", dto.beløp),
        )

    private fun parseBeløp(
        felt: String,
        verdiFelt: VerdiFelt<String>?,
    ): VerdiFelt<BigDecimal>? = verdiFelt?.let { parsePåkrevdBeløp(felt, it) }

    private fun parsePåkrevdBeløp(
        felt: String,
        verdiFelt: VerdiFelt<String>,
    ): VerdiFelt<BigDecimal> {
        val normalisertVerdi = verdiFelt.verdi.trim()
        if (!beløpsformat.matches(normalisertVerdi)) {
            ugyldigFelt(felt)
        }
        val verdi =
            BigDecimal(normalisertVerdi.replace(',', '.'))
                .setScale(2, RoundingMode.UNNECESSARY)
        return VerdiFelt(verdi = verdi, label = verdiFelt.label)
    }

    private fun ugyldigFelt(felt: String): Nothing = throw SøknadValideringException("Ugyldig felt i flyttesøknaden: $felt")

    private companion object {
        const val ANTALL_TILBUD = 2
        const val MAKS_NAVNELENGDE = 200
    }
}
