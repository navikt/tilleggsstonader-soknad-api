package no.nav.tilleggsstonader.soknad.person

import no.nav.tilleggsstonader.libs.log.logger
import no.nav.tilleggsstonader.soknad.kodeverk.KodeverkService
import no.nav.tilleggsstonader.soknad.person.dto.Adresse
import no.nav.tilleggsstonader.soknad.person.pdl.dto.Bostedsadresse
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlSøker
import no.nav.tilleggsstonader.soknad.util.TekstUtil.joinNotNullOrEmpty
import org.springframework.stereotype.Service

@Service
class AdresseMapper(
    private val kodeverkService: KodeverkService,
) {
    fun tilAdresse(pdlSøker: PdlSøker): Adresse = pdlSøker.bostedsadresse.firstOrNull().tilAdresse()

    fun tilFormatertAdresse(pdlSøker: PdlSøker): String {
        val adresse = pdlSøker.bostedsadresse.firstOrNull().tilAdresse()
        return joinNotNullOrEmpty(
            adresse.gateadresse,
            joinNotNullOrEmpty(adresse.postnummer, adresse.poststed),
            separator = ", ",
        ) ?: ""
    }

    private fun Bostedsadresse?.tilAdresse(): Adresse {
        if (this == null) {
            logger.info("Finner ikke bostedadresse")
            return Adresse()
        }
        return when {
            vegadresse != null ->
                Adresse(
                    land = "NOR", // Bør på sikt tillate utenlandske bostedsadresser også
                    gateadresse =
                        joinNotNullOrEmpty(
                            vegadresse.adressenavn,
                            vegadresse.husnummer,
                            vegadresse.husbokstav,
                            vegadresse.bruksenhetsnummer,
                        ),
                    postnummer = vegadresse.postnummer,
                    poststed = hentPoststed(vegadresse.postnummer),
                )

            matrikkeladresse != null ->
                Adresse(
                    land = "NOR",
                    gateadresse = matrikkeladresse.tilleggsnavn,
                    postnummer = matrikkeladresse.postnummer,
                    poststed = hentPoststed(matrikkeladresse.postnummer),
                )

            else -> Adresse().also { logger.info("Søker har hverken vegadresse eller matrikkeladresse") }
        }
    }

    private fun hentPoststed(postnummer: String?) = kodeverkService.hentPoststed(postnummer)
}
