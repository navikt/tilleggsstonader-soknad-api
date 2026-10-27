package no.nav.tilleggsstonader.soknad.soknad.flytting

import no.nav.tilleggsstonader.kontrakter.søknad.Dokument
import no.nav.tilleggsstonader.kontrakter.søknad.DokumentasjonFelt
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFelt
import no.nav.tilleggsstonader.kontrakter.søknad.JaNei
import no.nav.tilleggsstonader.kontrakter.søknad.SelectFelt
import no.nav.tilleggsstonader.kontrakter.søknad.Vedleggstype
import no.nav.tilleggsstonader.kontrakter.søknad.VerdiFelt
import no.nav.tilleggsstonader.kontrakter.søknad.flytting.Flyttemåte
import no.nav.tilleggsstonader.kontrakter.søknad.flytting.Flytteårsak
import no.nav.tilleggsstonader.soknad.soknad.SøknadMetadataDto
import no.nav.tilleggsstonader.soknad.soknad.SøknadTestUtil
import no.nav.tilleggsstonader.soknad.soknad.reiseTilSamling.AdresseDto
import java.util.UUID

object SøknadFlyttingUtil {
    fun søknadFlytting(flyttemåte: Flyttemåte): SøknadFlyttingDto =
        SøknadFlyttingDto(
            hovedytelse = SøknadTestUtil.hovedytelseDto(),
            flyttingDetaljer =
                FlyttingDetaljerDto(
                    årsak = EnumFelt("Hvorfor flytter du?", Flytteårsak.NY_JOBB, "Ny jobb", emptyList()),
                    flyttedato = felt("2026-03-01", "Flyttedato"),
                    adresse =
                        AdresseDto(
                            land = SelectFelt("Land", "NO", "Norge"),
                            gateadresse = felt("Mimes vei 1", "Gateadresse"),
                            postnummer = felt("5132", "Postnummer"),
                            poststed = felt("Nyborg", "Poststed"),
                        ),
                    skalFlytteFraFolkeregistrertAdresse =
                        EnumFelt("Flytter du fra din folkeregistrerte adresse?", JaNei.NEI, "Nei", emptyList()),
                    adresseDetSkalFlyttesFra =
                        AdresseDto(
                            land = SelectFelt("Land", "NO", "Norge"),
                            gateadresse = felt("Hjemveien 2", "Gateadresse"),
                            postnummer = felt("0123", "Postnummer"),
                            poststed = felt("Oslo", "Poststed"),
                        ),
                    flyttemåte = EnumFelt("Hvordan flytter du?", flyttemåte, flyttemåte.name, emptyList()),
                    utgifterDekketPåAnnenMåte = EnumFelt("Annen dekning?", JaNei.NEI, "Nei", emptyList()),
                    utgifterEgenFlytting =
                        if (flyttemåte == Flyttemåte.FLYTTER_SELV) {
                            UtgifterEgenFlyttingDto(
                                avstand = felt("120,25", "Avstand"),
                                hengerleie = felt("500", "Hengerleie"),
                                bom = felt("0", "Bom"),
                                parkering = felt("25.50", "Parkering"),
                                ferje = felt("100,25", "Ferje"),
                                annet = null,
                            )
                        } else {
                            null
                        },
                    flyttebyrå =
                        if (flyttemåte == Flyttemåte.FLYTTEBYRÅ) {
                            FlyttebyråDto(
                                listOf(
                                    FlyttebyråTilbudDto(felt("Flyttebyrå 1", "Navn"), felt("12000", "Beløp")),
                                    FlyttebyråTilbudDto(felt("Flyttebyrå 2", "Navn"), felt("10500,50", "Beløp")),
                                ),
                            )
                        } else {
                            null
                        },
                ),
            dokumentasjon =
                (
                    if (flyttemåte == Flyttemåte.FLYTTER_SELV) {
                        listOf(Vedleggstype.UTGIFTER_EGEN_FLYTTING)
                    } else {
                        listOf(Vedleggstype.TILBUD_FLYTTEBYRÅ_1, Vedleggstype.TILBUD_FLYTTEBYRÅ_2)
                    }
                ).mapIndexed { index, type ->
                    DokumentasjonFelt(
                        type = type,
                        label = type.tittel,
                        opplastedeVedlegg =
                            listOf(Dokument(UUID.fromString("00000000-0000-0000-0000-00000000000${index + 1}"), "vedlegg.pdf")),
                    )
                },
            søknadMetadata = SøknadMetadataDto("aabbccd"),
        )

    private fun felt(
        verdi: String,
        label: String,
    ) = VerdiFelt(verdi, label)
}
