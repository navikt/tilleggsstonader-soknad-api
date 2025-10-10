package no.nav.tilleggsstonader.soknad.soknad

import no.nav.tilleggsstonader.kontrakter.felles.Hovedytelse
import no.nav.tilleggsstonader.kontrakter.felles.ObjectMapperProvider.objectMapper
import no.nav.tilleggsstonader.kontrakter.felles.Skjematype
import no.nav.tilleggsstonader.kontrakter.søknad.DatoFelt
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFelt
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFlereValgFelt
import no.nav.tilleggsstonader.kontrakter.søknad.InnsendtSkjema
import no.nav.tilleggsstonader.kontrakter.søknad.JaNei
import no.nav.tilleggsstonader.kontrakter.søknad.SelectFelt
import no.nav.tilleggsstonader.kontrakter.søknad.VerdiFelt
import no.nav.tilleggsstonader.kontrakter.søknad.felles.TypePengestøtte
import no.nav.tilleggsstonader.kontrakter.søknad.felles.ÅrsakOppholdUtenforNorge
import no.nav.tilleggsstonader.soknad.infrastruktur.database.JsonWrapper
import no.nav.tilleggsstonader.soknad.kjøreliste.KjørelisteDto
import no.nav.tilleggsstonader.soknad.kjøreliste.KjørelisteMapper
import no.nav.tilleggsstonader.soknad.person.dto.Barn
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.BarnetilsynMapper
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.SøknadBarnetilsynDto
import no.nav.tilleggsstonader.soknad.soknad.domene.Skjema
import no.nav.tilleggsstonader.soknad.soknad.læremidler.LæremidlerMapper
import no.nav.tilleggsstonader.soknad.soknad.læremidler.SøknadLæremidlerDto
import java.time.LocalDate
import java.time.LocalDateTime

object SøknadTestUtil {
    private val mottattTidspunkt = LocalDateTime.of(2023, 1, 1, 12, 13, 0)

    fun lagKjøreliste(kjørelisteDto: KjørelisteDto): Skjema =
        lagSkjema(Skjematype.DAGLIG_REISE_KJØRELISTE, lagInnsendtSkjema(kjørelisteDto))

    fun lagSøknad(søknadDto: SøknadBarnetilsynDto): Skjema = lagSkjema(Skjematype.SØKNAD_BARNETILSYN, lagInnsendtSkjema(søknadDto))

    fun lagSøknad(søknadDto: SøknadLæremidlerDto): Skjema = lagSkjema(Skjematype.SØKNAD_LÆREMIDLER, lagInnsendtSkjema(søknadDto))

    fun lagInnsendtSkjema(kjørelisteDto: KjørelisteDto) = KjørelisteMapper.map("25518735813", mottattTidspunkt, kjørelisteDto)

    fun lagInnsendtSkjema(søknadDto: SøknadBarnetilsynDto) =
        BarnetilsynMapper().map("25518735813", mottattTidspunkt, mapBarn(søknadDto), søknadDto)

    fun lagInnsendtSkjema(søknadDto: SøknadLæremidlerDto) = LæremidlerMapper().map("25518735813", mottattTidspunkt, søknadDto)

    fun lagSkjema(
        skjematype: Skjematype,
        innsendtSkjema: InnsendtSkjema<*>,
    ): Skjema =
        Skjema(
            skjemaJson = JsonWrapper(objectMapper.writeValueAsString(innsendtSkjema)),
            type = skjematype,
            personIdent = innsendtSkjema.ident,
            opprettetTid = LocalDateTime.now(),
            frontendGitHash = "aabbccd",
        )

    fun mapBarn(søknad: SøknadBarnetilsynDto): Map<String, Barn> =
        søknad.barnMedBarnepass.associate {
            it.ident to
                Barn(
                    ident = it.ident,
                    fornavn = "Fornavn",
                    visningsnavn = "Fornavn Etternavn",
                    fødselsdato = LocalDate.of(2023, 1, 1),
                    alder = 4,
                )
        }

    private fun arbeidOgOppholdDto() =
        ArbeidOgOppholdDto(
            jobberIAnnetLand = EnumFelt("Jobber du i et annet land enn Norge?", JaNei.JA, "Ja", emptyList()),
            jobbAnnetLand = SelectFelt("Hvilket land jobber du i?", "SWE", "Sverige"),
            harPengestøtteAnnetLand =
                EnumFlereValgFelt(
                    "Mottar du pengestøtte fra et annet land enn Norge?",
                    listOf(VerdiFelt(TypePengestøtte.SYKEPENGER, "Sykepenger")),
                    listOf("Sykepenger", "Annet"),
                ),
            pengestøtteAnnetLand = SelectFelt("Hvilket land mottar du pengestøtte fra?", "SWE", "Sverige"),
            harOppholdUtenforNorgeSiste12mnd =
                EnumFelt(
                    "Jobber du i et annet land enn Norge?",
                    JaNei.JA,
                    "Ja",
                    emptyList(),
                ),
            oppholdUtenforNorgeSiste12mnd = listOf(oppholdUtenforNorgeDto(), oppholdUtenforNorgeDto()),
            harOppholdUtenforNorgeNeste12mnd =
                EnumFelt(
                    "Jobber du i et annet land enn Norge?",
                    JaNei.JA,
                    "Ja",
                    emptyList(),
                ),
            oppholdUtenforNorgeNeste12mnd = listOf(oppholdUtenforNorgeDto()),
        )

    fun hovedytelseDto() =
        HovedytelseDto(
            ytelse =
                EnumFlereValgFelt(
                    label = "Hovedytelse?",
                    verdier =
                        listOf(
                            VerdiFelt(Hovedytelse.AAP, "AAP"),
                            VerdiFelt(Hovedytelse.OVERGANGSSTØNAD, "Overgangsstønad"),
                        ),
                    alternativer = listOf("Alt1", "Alt2"),
                ),
            arbeidOgOpphold = arbeidOgOppholdDto(),
        )

    private fun oppholdUtenforNorgeDto() =
        OppholdUtenforNorgeDto(
            land = SelectFelt("Hvilket land har du oppholdt deg i?", "SWE", "Sverige"),
            årsak =
                EnumFlereValgFelt(
                    "Hva gjorde du i dette landet?",
                    listOf(VerdiFelt(ÅrsakOppholdUtenforNorge.JOBB, "Jobb")),
                    alternativer = listOf("Jobb", "Studier"),
                ),
            fom = DatoFelt("Fom", LocalDate.of(2024, 1, 1)),
            tom = DatoFelt("Fom", LocalDate.of(2024, 1, 1)),
        )
}
