package no.nav.tilleggsstonader.soknad.soknad.barnetilsyn

import no.nav.tilleggsstonader.kontrakter.søknad.DatoFelt
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFelt
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFlereValgFelt
import no.nav.tilleggsstonader.kontrakter.søknad.JaNei
import no.nav.tilleggsstonader.kontrakter.søknad.SelectFelt
import no.nav.tilleggsstonader.kontrakter.søknad.VerdiFelt
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.ArbeidOgOpphold
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.TypePengestøtte
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.OppholdUtenforNorge
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.ÅrsakOppholdUtenforNorge
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.ArbeidOgOppholdMapper.mapArbeidOgOpphold
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ArbeidOgOppholdMapperTest {

    @Test
    fun `skal mappe til null hvis alle hovedspørsmål er null`() {
        assertThat(mapArbeidOgOpphold(arbeidOgOppholdDto())).isNull()
    }

    @Test
    fun `skal mappe verdi hvis hovedspørsmål finnes`() {
        assertThat(mapArbeidOgOpphold(arbeidOgOppholdDto(jobberIAnnetLand = enumJaNei()))).isNotNull()
        assertThat(mapArbeidOgOpphold(arbeidOgOppholdDto(harPengestøtteAnnetLand = harPengestøtteAnnetLand()))).isNotNull()
        assertThat(mapArbeidOgOpphold(arbeidOgOppholdDto(harOppholdUtenforNorgeSiste12mnd = enumJaNei()))).isNotNull()
    }

    @Test
    fun `skal mappe alle verdier`() {
        val verdi = mapArbeidOgOpphold(
            ArbeidOgOppholdDto(
                jobberIAnnetLand = enumJaNei("jobberIAnnetLand"),
                jobbAnnetLand = SelectFelt("jobbAnnetLand", "SWE", "Sverige"),
                harPengestøtteAnnetLand = harPengestøtteAnnetLand(),
                pengestøtteAnnetLand = SelectFelt("pengestøtteAnnetLand", "SWE", "Sverige"),
                harOppholdUtenforNorgeSiste12mnd = enumJaNei("harOppholdUtenforNorgeSiste12mnd"),
                oppholdUtenforNorgeSiste12mnd = listOf(
                    OppholdUtenforNorgeDto(
                        SelectFelt("land", "SWE", "Sverige"),
                        årsakJobb(),
                        DatoFelt("Fom", LocalDate.of(2024, 1, 1)),
                        DatoFelt("Tom", LocalDate.of(2024, 1, 2)),
                    ),
                ),
                harOppholdUtenforNorgeNeste12mnd = enumJaNei("harOppholdUtenforNorgeNeste12mnd"),
                oppholdUtenforNorgeNeste12mnd = listOf(
                    OppholdUtenforNorgeDto(
                        SelectFelt("land", "FIN", "Finland"),
                        årsakFerie(),
                        DatoFelt("Fom", LocalDate.of(2024, 1, 1)),
                        DatoFelt("Tom", LocalDate.of(2024, 1, 2)),
                    ),
                ),
            ),
        )
        val expected = ArbeidOgOpphold(
            jobberIAnnetLand = enumJaNei("jobberIAnnetLand"),
            jobbAnnetLand = SelectFelt("jobbAnnetLand", "SWE", "Sverige"),
            harPengestøtteAnnetLand = harPengestøtteAnnetLand(),
            pengestøtteAnnetLand = SelectFelt("pengestøtteAnnetLand", "SWE", "Sverige"),
            harOppholdUtenforNorgeSiste12mnd = enumJaNei("harOppholdUtenforNorgeSiste12mnd"),
            oppholdUtenforNorgeSiste12mnd = listOf(
                OppholdUtenforNorge(
                    SelectFelt("land", "SWE", "Sverige"),
                    årsakJobb(),
                    DatoFelt("Fom", LocalDate.of(2024, 1, 1)),
                    DatoFelt("Tom", LocalDate.of(2024, 1, 2)),
                ),
            ),
            harOppholdUtenforNorgeNeste12mnd = enumJaNei("harOppholdUtenforNorgeNeste12mnd"),
            oppholdUtenforNorgeNeste12mnd = listOf(
                OppholdUtenforNorge(
                    SelectFelt("land", "FIN", "Finland"),
                    årsakFerie(),
                    DatoFelt("Fom", LocalDate.of(2024, 1, 1)),
                    DatoFelt("Tom", LocalDate.of(2024, 1, 2)),
                ),
            ),
        )
        assertThat(verdi).isEqualTo(expected)
    }

    private fun årsakJobb() = EnumFlereValgFelt(
        "årsak",
        listOf(VerdiFelt(ÅrsakOppholdUtenforNorge.JOBB, "Jobb")),
        emptyList(),
    )

    private fun årsakFerie() = EnumFlereValgFelt(
        "årsak",
        listOf(VerdiFelt(ÅrsakOppholdUtenforNorge.FERIE, "Ferie")),
        emptyList(),
    )

    private fun harPengestøtteAnnetLand() = EnumFlereValgFelt(
        label = "harPengestøtteAnnetLand",
        verdier = listOf(VerdiFelt(TypePengestøtte.ANNEN_PENGESTØTTE, "Annen pengestøtte")),
        alternativer = emptyList(),
    )

    private fun enumJaNei(
        label: String = "",
        verdi: JaNei = JaNei.JA,
    ) = EnumFelt(label, verdi, "", emptyList())

    private fun arbeidOgOppholdDto(
        jobberIAnnetLand: EnumFelt<JaNei>? = null,
        jobbAnnetLand: SelectFelt<String>? = null,
        harPengestøtteAnnetLand: EnumFlereValgFelt<TypePengestøtte>? = null,
        pengestøtteAnnetLand: SelectFelt<String>? = null,
        harOppholdUtenforNorgeSiste12mnd: EnumFelt<JaNei>? = null,
        oppholdUtenforNorgeSiste12mnd: List<OppholdUtenforNorgeDto> = emptyList(),
        harOppholdUtenforNorgeNeste12mnd: EnumFelt<JaNei>? = null,
        oppholdUtenforNorgeNeste12mnd: List<OppholdUtenforNorgeDto> = emptyList(),
    ) = ArbeidOgOppholdDto(
        jobberIAnnetLand = jobberIAnnetLand,
        jobbAnnetLand = jobbAnnetLand,
        harPengestøtteAnnetLand = harPengestøtteAnnetLand,
        pengestøtteAnnetLand = pengestøtteAnnetLand,
        harOppholdUtenforNorgeSiste12mnd = harOppholdUtenforNorgeSiste12mnd,
        oppholdUtenforNorgeSiste12mnd = oppholdUtenforNorgeSiste12mnd,
        harOppholdUtenforNorgeNeste12mnd = harOppholdUtenforNorgeNeste12mnd,
        oppholdUtenforNorgeNeste12mnd = oppholdUtenforNorgeNeste12mnd,
    )
}
