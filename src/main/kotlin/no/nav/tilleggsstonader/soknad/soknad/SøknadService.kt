package no.nav.tilleggsstonader.soknad.soknad

import no.nav.tilleggsstonader.kontrakter.felles.ObjectMapperProvider
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.kontrakter.søknad.Søknadsskjema
import no.nav.tilleggsstonader.soknad.infrastruktur.database.JsonWrapper
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.BarnetilsynMapper
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.SøknadBarnetilsynDto
import no.nav.tilleggsstonader.soknad.soknad.domene.Søknad
import no.nav.tilleggsstonader.soknad.soknad.domene.SøknadRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class SøknadService(
    private val søknadRepository: SøknadRepository,
    private val barnetilsynMapper: BarnetilsynMapper,
) {

    @Transactional
    fun lagreSøknad(
        personIdent: String,
        mottattTidspunkt: LocalDateTime,
        søknad: SøknadBarnetilsynDto,
    ) {
        val type = Stønadstype.BARNETILSYN
        lagreSøknad(
            type = type,
            personIdent = personIdent,
            mottattTidspunkt = mottattTidspunkt,
            søknad = barnetilsynMapper.map(søknad),
        )
    }

    private fun <T> lagreSøknad(
        type: Stønadstype,
        personIdent: String,
        mottattTidspunkt: LocalDateTime,
        søknad: T,
    ) {
        val søknadsskjema = Søknadsskjema(personIdent, mottattTidspunkt, søknad)
        søknadRepository.insert(
            Søknad(
                type = type,
                personIdent = personIdent,
                søknadJson = JsonWrapper(ObjectMapperProvider.objectMapper.writeValueAsString(søknadsskjema)),
            ),
        )
        // TODO lagre vedlegg
    }
}
