package no.nav.tilleggsstonader.soknad.soknad.flytting

import no.nav.tilleggsstonader.kontrakter.søknad.DokumentasjonFelt
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFelt
import no.nav.tilleggsstonader.kontrakter.søknad.JaNei
import no.nav.tilleggsstonader.kontrakter.søknad.VerdiFelt
import no.nav.tilleggsstonader.kontrakter.søknad.flytting.Flyttemåte
import no.nav.tilleggsstonader.kontrakter.søknad.flytting.Flytteårsak
import no.nav.tilleggsstonader.soknad.soknad.HovedytelseDto
import no.nav.tilleggsstonader.soknad.soknad.SøknadMetadataDto
import no.nav.tilleggsstonader.soknad.soknad.reiseTilSamling.AdresseDto

data class SøknadFlyttingDto(
    val hovedytelse: HovedytelseDto,
    val flyttingDetaljer: FlyttingDetaljerDto?,
    val dokumentasjon: List<DokumentasjonFelt>,
    val søknadMetadata: SøknadMetadataDto,
)

data class FlyttingDetaljerDto(
    val årsak: EnumFelt<Flytteårsak>,
    val flyttedato: VerdiFelt<String>,
    val adresse: AdresseDto,
    val flyttemåte: EnumFelt<Flyttemåte>,
    val utgifterDekketPåAnnenMåte: EnumFelt<JaNei>,
    val utgifterEgenFlytting: UtgifterEgenFlyttingDto?,
    val flyttebyrå: FlyttebyråDto?,
    val skalFlytteFraFolkeregistrertAdresse: EnumFelt<JaNei>,
    val adresseDetSkalFlyttesFra: AdresseDto,
)

data class UtgifterEgenFlyttingDto(
    val avstand: VerdiFelt<String>,
    val hengerleie: VerdiFelt<String>?,
    val bom: VerdiFelt<String>?,
    val parkering: VerdiFelt<String>?,
    val ferje: VerdiFelt<String>?,
    val annet: VerdiFelt<String>?,
)

data class FlyttebyråDto(
    val tilbud: List<FlyttebyråTilbudDto>,
)

data class FlyttebyråTilbudDto(
    val navn: VerdiFelt<String>,
    val beløp: VerdiFelt<String>,
)
