package no.nav.tilleggsstonader.soknad.soknad.flytting

import no.nav.tilleggsstonader.kontrakter.søknad.EnumFelt
import no.nav.tilleggsstonader.kontrakter.søknad.JaNei
import no.nav.tilleggsstonader.kontrakter.søknad.SelectFelt
import no.nav.tilleggsstonader.kontrakter.søknad.VerdiFelt
import no.nav.tilleggsstonader.kontrakter.søknad.flytting.Flyttemåte
import no.nav.tilleggsstonader.kontrakter.søknad.flytting.Flytteårsak
import no.nav.tilleggsstonader.soknad.soknad.SøknadMetadataDto
import no.nav.tilleggsstonader.soknad.soknad.SøknadTestUtil
import no.nav.tilleggsstonader.soknad.soknad.SøknadValideringException
import no.nav.tilleggsstonader.soknad.soknad.reiseTilSamling.AdresseDto
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime

class FlyttingMapperTest {
    private val mapper = FlyttingMapper()

    @Test
    fun `mapper hovedytelse til kontrakten`() {
        val søknad = søknad(flyttemåte = Flyttemåte.FLYTTER_SELV, utgifter = null)

        val resultat = mapper.map("ident", LocalDateTime.now(), søknad.copy(flyttingDetaljer = null))

        assertThat(resultat.skjema.hovedytelse.hovedytelse).isEqualTo(søknad.hovedytelse.ytelse)
    }

    @Test
    fun `mapper beløp med desimalkomma til kontrakten`() {
        val søknad =
            søknad(
                flyttemåte = Flyttemåte.FLYTTEBYRÅ,
                flyttebyrå =
                    FlyttebyråDto(
                        tilbud =
                            listOf(
                                tilbud(navn = "Byrå A", beløp = "1234,5"),
                                tilbud(navn = "Byrå B", beløp = "1100.50"),
                            ),
                    ),
            )

        val resultat = mapper.map("ident", LocalDateTime.now(), søknad)
        val flyttebyrå = resultat.skjema.flyttingDetaljer?.flyttebyrå

        assertThat(flyttebyrå?.tilbud?.map { it.beløp.verdi })
            .containsExactly(BigDecimal("1234.50"), BigDecimal("1100.50"))
    }

    @Test
    fun `mapper utgifter ved egen flytting til BigDecimal`() {
        val søknad =
            søknad(
                flyttemåte = Flyttemåte.FLYTTER_SELV,
                utgifter =
                    UtgifterEgenFlyttingDto(
                        avstand = felt("120,25"),
                        hengerleie = felt("0"),
                        leiebil = felt("1000"),
                        bom = felt("15.50"),
                        parkering = null,
                        ferje = null,
                        annet = null,
                    ),
            )

        val resultat = mapper.map("ident", LocalDateTime.now(), søknad)
        val utgifter = resultat.skjema.flyttingDetaljer?.utgifterEgenFlytting

        assertThat(utgifter?.avstand?.verdi).isEqualTo(BigDecimal("120.25"))
        assertThat(utgifter?.hengerleie?.verdi).isEqualTo(BigDecimal("0.00"))
        assertThat(utgifter?.leiebil?.verdi).isEqualTo(BigDecimal("1000.00"))
        assertThat(utgifter?.bom?.verdi).isEqualTo(BigDecimal("15.50"))
        assertThat(utgifter?.parkering).isNull()
    }

    @Test
    fun `mapper adressen brukeren flytter fra`() {
        val søknad =
            søknad(
                flyttemåte = Flyttemåte.FLYTTER_SELV,
                utgifter =
                    UtgifterEgenFlyttingDto(
                        avstand = felt("1"),
                        hengerleie = null,
                        leiebil = null,
                        bom = null,
                        parkering = null,
                        ferje = null,
                        annet = null,
                    ),
                skalFlytteFraFolkeregistrertAdresse = EnumFelt("Flytter fra folkeregistrert adresse?", JaNei.NEI, "Nei", emptyList()),
                adresseDetSkalFlyttesFra =
                    AdresseDto(
                        land = SelectFelt("Land", "NOR", "Norge"),
                        gateadresse = felt("Hjemveien 2"),
                        postnummer = felt("0123"),
                        poststed = felt("Oslo"),
                    ),
            )

        val resultat = mapper.map("ident", LocalDateTime.now(), søknad)
        val detaljer = resultat.skjema.flyttingDetaljer

        assertThat(detaljer?.skalFlytteFraFolkeregistrertAdresse?.verdi).isEqualTo(JaNei.NEI)
        assertThat(detaljer?.adresseDetSkalFlyttesFra?.gateadresse?.verdi).isEqualTo("Hjemveien 2")
        assertThat(detaljer?.adresseDetSkalFlyttesFra?.postnummer?.verdi).isEqualTo("0123")
        assertThat(detaljer?.adresseDetSkalFlyttesFra?.poststed?.verdi).isEqualTo("Oslo")
        assertThat(detaljer?.adresseDetSkalFlyttesFra?.land?.verdi).isEqualTo("NOR")
    }

    @Test
    fun `mapper folkeregistrert adresse når brukeren svarer ja`() {
        val søknad =
            søknad(
                flyttemåte = Flyttemåte.FLYTTER_SELV,
                utgifter =
                    UtgifterEgenFlyttingDto(
                        avstand = felt("1"),
                        hengerleie = null,
                        leiebil = null,
                        bom = null,
                        parkering = null,
                        ferje = null,
                        annet = null,
                    ),
                skalFlytteFraFolkeregistrertAdresse = EnumFelt("Flytter fra folkeregistrert adresse?", JaNei.JA, "Ja", emptyList()),
                adresseDetSkalFlyttesFra =
                    AdresseDto(
                        land = SelectFelt("Land", "NOR", "Norge"),
                        gateadresse = felt("Hildes vei"),
                        postnummer = felt("0100"),
                        poststed = felt("Oslo"),
                    ),
            )

        val detaljer = mapper.map("ident", LocalDateTime.now(), søknad).skjema.flyttingDetaljer

        assertThat(detaljer?.skalFlytteFraFolkeregistrertAdresse?.verdi).isEqualTo(JaNei.JA)
        assertThat(detaljer?.adresseDetSkalFlyttesFra?.gateadresse?.verdi).isEqualTo("Hildes vei")
    }

    @Test
    fun `avviser beløp med ugyldig format uten å gjengi verdien`() {
        listOf("-1", "1.234", "1e8", "1.2.3").forEach { verdi ->
            assertThatThrownBy {
                mapper.map(
                    "ident",
                    LocalDateTime.now(),
                    søknad(
                        flyttemåte = Flyttemåte.FLYTTER_SELV,
                        utgifter =
                            UtgifterEgenFlyttingDto(
                                avstand = felt("1"),
                                hengerleie = felt(verdi),
                                leiebil = null,
                                bom = null,
                                parkering = null,
                                ferje = null,
                                annet = null,
                            ),
                    ),
                )
            }.isInstanceOf(SøknadValideringException::class.java)
                .hasMessageContaining("hengerleie")
                .hasMessageNotContaining(verdi)
        }
    }

    @Test
    fun `avviser ugyldig avstand og ufullstendige tilbud`() {
        listOf("0", "-1", "1.234").forEach { avstand ->
            assertThatThrownBy {
                mapper.map(
                    "ident",
                    LocalDateTime.now(),
                    søknad(
                        flyttemåte = Flyttemåte.FLYTTER_SELV,
                        utgifter =
                            UtgifterEgenFlyttingDto(
                                avstand = felt(avstand),
                                hengerleie = null,
                                leiebil = null,
                                bom = null,
                                parkering = null,
                                ferje = null,
                                annet = null,
                            ),
                    ),
                )
            }.isInstanceOf(SøknadValideringException::class.java)
        }

        assertThatThrownBy {
            mapper.map(
                "ident",
                LocalDateTime.now(),
                søknad(
                    flyttemåte = Flyttemåte.FLYTTEBYRÅ,
                    flyttebyrå =
                        FlyttebyråDto(
                            tilbud = emptyList(),
                        ),
                ),
            )
        }.isInstanceOf(SøknadValideringException::class.java)

        assertThatThrownBy {
            mapper.map(
                "ident",
                LocalDateTime.now(),
                søknad(
                    flyttemåte = Flyttemåte.FLYTTEBYRÅ,
                    flyttebyrå =
                        FlyttebyråDto(
                            tilbud = listOf(tilbud("A", "100")),
                        ),
                ),
            )
        }.isInstanceOf(SøknadValideringException::class.java)
    }

    @Test
    fun `godtar store beløp uten en egen siffergrense`() {
        val stortBeløp = "9".repeat(400)
        val søknad =
            søknad(
                flyttemåte = Flyttemåte.FLYTTEBYRÅ,
                flyttebyrå =
                    FlyttebyråDto(
                        tilbud = listOf(tilbud("Byrå A", stortBeløp), tilbud("Byrå B", "100")),
                    ),
            )

        val resultat = mapper.map("ident", LocalDateTime.now(), søknad)

        assertThat(
            resultat.skjema.flyttingDetaljer
                ?.flyttebyrå
                ?.tilbud
                ?.first()
                ?.beløp
                ?.verdi,
        ).isEqualTo(BigDecimal(stortBeløp).setScale(2))
    }

    @Test
    fun `lagrer ikke data fra inaktiv flyttemåte`() {
        val søknad =
            søknad(
                flyttemåte = Flyttemåte.FLYTTER_SELV,
                utgifter =
                    UtgifterEgenFlyttingDto(
                        avstand = felt("10"),
                        hengerleie = null,
                        leiebil = null,
                        bom = null,
                        parkering = null,
                        ferje = null,
                        annet = null,
                    ),
                flyttebyrå =
                    FlyttebyråDto(
                        tilbud = listOf(tilbud("Ufullstendig", "ugyldig"), tilbud("", "")),
                    ),
            )

        val resultat = mapper.map("ident", LocalDateTime.now(), søknad)

        assertThat(resultat.skjema.flyttingDetaljer?.flyttebyrå).isNull()
    }

    private fun søknad(
        flyttemåte: Flyttemåte,
        flyttebyrå: FlyttebyråDto? = null,
        utgifter: UtgifterEgenFlyttingDto? = null,
        skalFlytteFraFolkeregistrertAdresse: EnumFelt<JaNei> =
            EnumFelt("Flytter fra folkeregistrert adresse?", JaNei.NEI, "Nei", emptyList()),
        adresseDetSkalFlyttesFra: AdresseDto =
            AdresseDto(land = null, gateadresse = felt("Hjemveien 2"), postnummer = felt("0123"), poststed = felt("Oslo")),
    ) = SøknadFlyttingDto(
        hovedytelse = SøknadTestUtil.hovedytelseDto(),
        flyttingDetaljer =
            FlyttingDetaljerDto(
                årsak = EnumFelt("Årsak", Flytteårsak.AKTIVITET, "Aktivitet", emptyList()),
                flyttedato = felt("2026-03-01"),
                adresse = AdresseDto(land = null, gateadresse = null, postnummer = null, poststed = null),
                flyttemåte = EnumFelt("Flyttemåte", flyttemåte, flyttemåte.name, listOf(flyttemåte.name)),
                utgifterDekketPåAnnenMåte = EnumFelt("Annen dekning", JaNei.NEI, "Nei", emptyList()),
                utgifterEgenFlytting = utgifter,
                flyttebyrå = flyttebyrå,
                skalFlytteFraFolkeregistrertAdresse = skalFlytteFraFolkeregistrertAdresse,
                adresseDetSkalFlyttesFra = adresseDetSkalFlyttesFra,
            ),
        dokumentasjon = emptyList(),
        søknadMetadata = SøknadMetadataDto(søknadFrontendGitHash = "test"),
    )

    private fun tilbud(
        navn: String,
        beløp: String,
    ) = FlyttebyråTilbudDto(
        navn = felt(navn),
        beløp = felt(beløp),
    )

    private fun felt(verdi: String) = VerdiFelt(verdi = verdi, label = "Testfelt")
}
