package no.nav.tilleggsstonader.soknad.soknad

import no.nav.tilleggsstonader.kontrakter.felles.ObjectMapperProvider.objectMapper
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.kontrakter.søknad.Søknadsskjema
import no.nav.tilleggsstonader.soknad.infrastruktur.database.JsonWrapper
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.BarnetilsynMapper
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.SøknadBarnetilsynDto
import no.nav.tilleggsstonader.soknad.soknad.domene.Søknad
import java.time.LocalDateTime

object SøknadTestUtil {

    private val mottattTidspunkt = LocalDateTime.of(2023, 1, 1, 12, 13, 0)

    fun lagSøknad(søknadDto: SøknadBarnetilsynDto): Søknad =
        lagSøknad(Stønadstype.BARNETILSYN, lagSøknadsksjema(søknadDto))

    fun lagSøknadsksjema(søknadDto: SøknadBarnetilsynDto) =
        BarnetilsynMapper().map("ident", mottattTidspunkt, søknadDto)

    fun lagSøknad(stønadstype: Stønadstype, søknadsskjema: Søknadsskjema<*>): Søknad {
        return Søknad(
            søknadJson = JsonWrapper(objectMapper.writeValueAsString(søknadsskjema)),
            type = stønadstype,
            personIdent = "1",
            opprettetTid = LocalDateTime.now(),
        )
    }
}
