package no.nav.tilleggsstonader.soknad.person

import no.nav.tilleggsstonader.soknad.kodeverk.KodeverkService
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlSøker
import no.nav.tilleggsstonader.soknad.person.pdl.dto.Vegadresse
import no.nav.tilleggsstonader.soknad.util.TekstUtil.joinNotNullOrEmpty
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AdresseMapper(
    private val kodeverkService: KodeverkService,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun tilFormatertAdresse(pdlSøker: PdlSøker): String {
        val bosted = pdlSøker.bostedsadresse.firstOrNull()
        return when {
            bosted == null -> "".also { logger.info("Finner ikke bostedadresse") }

            bosted.vegadresse != null -> {
                tilFormatertAdresse(bosted.vegadresse)
            }

            bosted.matrikkeladresse != null -> joinNotNullOrEmpty(
                bosted.matrikkeladresse.tilleggsnavn,
                joinNotNullOrEmpty(
                    bosted.matrikkeladresse.postnummer,
                    hentPoststed(bosted.matrikkeladresse.postnummer),
                ),
                separator = ", ",
            ) ?: ""

            else -> "".also { logger.info("Søker har hverken vegadresse eller matrikkeladresse") }
        }
    }

    private fun tilFormatertAdresse(vegadresse: Vegadresse): String =
        joinNotNullOrEmpty(
            joinNotNullOrEmpty(
                vegadresse.adressenavn,
                vegadresse.husnummer,
                vegadresse.husbokstav,
                vegadresse.bruksenhetsnummer,
            ),
            joinNotNullOrEmpty(
                vegadresse.postnummer,
                hentPoststed(vegadresse.postnummer),
            ),
            separator = ", ",
        ) ?: ""

    private fun hentPoststed(postnummer: String?) =
        kodeverkService.hentPoststed(postnummer)
}
