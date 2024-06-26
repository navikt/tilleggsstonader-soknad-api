package no.nav.tilleggsstonader.soknad.soknad

import no.nav.tilleggsstonader.kontrakter.felles.ObjectMapperProvider.objectMapper
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.kontrakter.søknad.Søknadsskjema
import no.nav.tilleggsstonader.libs.utils.osloNow
import no.nav.tilleggsstonader.soknad.infrastruktur.database.JsonWrapper
import no.nav.tilleggsstonader.soknad.person.dto.Barn
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.BarnetilsynMapper
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.SøknadBarnetilsynDto
import no.nav.tilleggsstonader.soknad.soknad.domene.Søknad
import java.time.LocalDate
import java.time.LocalDateTime

object SøknadTestUtil {

    private val mottattTidspunkt = LocalDateTime.of(2023, 1, 1, 12, 13, 0)

    fun lagSøknad(søknadDto: SøknadBarnetilsynDto): Søknad =
        lagSøknad(Stønadstype.BARNETILSYN, lagSøknadsksjema(søknadDto))

    fun lagSøknadsksjema(søknadDto: SøknadBarnetilsynDto) =
        BarnetilsynMapper().map("25518735813", mottattTidspunkt, mapBarn(søknadDto), søknadDto)

    fun lagSøknad(stønadstype: Stønadstype, søknadsskjema: Søknadsskjema<*>): Søknad {
        return Søknad(
            søknadJson = JsonWrapper(objectMapper.writeValueAsString(søknadsskjema)),
            type = stønadstype,
            personIdent = søknadsskjema.ident,
            opprettetTid = osloNow(),
        )
    }

    fun mapBarn(søknad: SøknadBarnetilsynDto): Map<String, Barn> {
        return søknad.barnMedBarnepass.associate {
            it.ident to Barn(
                ident = it.ident,
                fornavn = "Fornavn",
                visningsnavn = "Fornavn Etternavn",
                fødselsdato = LocalDate.of(2023, 1, 1),
                alder = 4,
            )
        }
    }
}
