package no.nav.tilleggsstonader.soknad.soknad

import no.nav.tilleggsstonader.kontrakter.felles.ObjectMapperProvider.objectMapper
import no.nav.tilleggsstonader.kontrakter.felles.Språkkode
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.kontrakter.søknad.Søknadsskjema
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.SøknadsskjemaBarnetilsyn
import no.nav.tilleggsstonader.soknad.infrastruktur.database.JsonWrapper
import no.nav.tilleggsstonader.soknad.person.dto.Barn
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.BarnetilsynMapper
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.SøknadBarnetilsynDto
import no.nav.tilleggsstonader.soknad.soknad.domene.Søknad
import java.time.LocalDate
import java.time.LocalDateTime

object SøknadTestUtil {

    fun lagSøknad(søknadDto: SøknadBarnetilsynDto): Søknad =
        lagSøknad(BarnetilsynMapper().map(søknadDto, mapBarn(søknadDto), Språkkode.NB))

    fun lagSøknad(søknadsskjema: SøknadsskjemaBarnetilsyn): Søknad =
        lagSøknad(Stønadstype.BARNETILSYN, søknadsskjema)

    fun lagSøknad(stønadstype: Stønadstype, søknadsskjema: Any): Søknad {
        when (søknadsskjema) {
            is SøknadsskjemaBarnetilsyn -> {}
            else -> error("Har ikke mappet lagring av $søknadsskjema")
        }
        return Søknad(
            søknadJson = JsonWrapper(objectMapper.writeValueAsString(lagSøknadsskjema(søknadsskjema))),
            type = stønadstype,
            personIdent = "1",
            opprettetTid = LocalDateTime.now(),
        )
    }

    fun lagSøknadsskjema(søknad: Any) = Søknadsskjema(
        "ident",
        LocalDateTime.of(2023, 1, 1, 12, 13, 0),
        Språkkode.NB,
        søknad,
    )

    fun mapBarn(søknad: SøknadBarnetilsynDto): Map<String, Barn> {
        return søknad.barnMedBarnepass.associate {
            it.ident to Barn(
                ident = it.ident,
                navn = "Fornavn Etternavn",
                fødselsdato = LocalDate.of(2023, 1, 1),
                alder = 4,
            )
        }
    }
}
