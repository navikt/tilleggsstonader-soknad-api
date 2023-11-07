package no.nav.tilleggsstonader.soknad.person.pdl

import no.nav.tilleggsstonader.soknad.infrastruktur.config.SecureLogger.secureLogger
import no.nav.tilleggsstonader.soknad.person.pdl.dto.Adressebeskyttelse
import no.nav.tilleggsstonader.soknad.person.pdl.dto.AdressebeskyttelseGradering
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlBolkResponse
import no.nav.tilleggsstonader.soknad.person.pdl.dto.PdlResponse
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders

val logger: Logger = LoggerFactory.getLogger(PdlClient::class.java)

object PdlUtil {
    val httpHeaders = HttpHeaders().apply {
        add("behandlingsnummer", "B289")
    }

    val søkerQuery = graphqlQuery("/pdl/søker.graphql")

    val barnQuery = graphqlQuery("/pdl/barn.graphql")

    private fun graphqlQuery(path: String) = PdlUtil::class.java.getResource(path)!!
        .readText()
        .graphqlCompatible()

    private fun String.graphqlCompatible(): String {
        return StringUtils.normalizeSpace(this.replace("\n", ""))
    }
}

fun List<Adressebeskyttelse>.gradering(): AdressebeskyttelseGradering =
    this.singleOrNull()?.gradering ?: AdressebeskyttelseGradering.UGRADERT

fun List<Adressebeskyttelse>.erStrengtFortrolig(): Boolean {
    val gradering = gradering()
    return gradering == AdressebeskyttelseGradering.STRENGT_FORTROLIG ||
        gradering == AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND
}

inline fun <reified DATA : Any, reified RESULT : Any> feilsjekkOgReturnerData(
    ident: String?,
    pdlResponse: PdlResponse<DATA>,
    dataMapper: (DATA) -> RESULT?,
): RESULT {
    if (pdlResponse.harFeil()) {
        if (pdlResponse.errors?.any { it.extensions?.notFound() == true } == true) {
            throw PdlNotFoundException()
        }
        secureLogger.error("Feil ved henting av ${RESULT::class} fra PDL: ${pdlResponse.errorMessages()}")
        throw PdlRequestException("Feil ved henting av ${RESULT::class} fra PDL. Se secure logg for detaljer.")
    }
    if (pdlResponse.harAdvarsel()) {
        logger.warn("Advarsel ved henting av ${RESULT::class} fra PDL. Se securelogs for detaljer.")
        secureLogger.warn("Advarsel ved henting av ${RESULT::class} fra PDL: ${pdlResponse.extensions?.warnings}")
    }
    val data = dataMapper.invoke(pdlResponse.data)
    if (data == null) {
        val errorMelding = if (ident != null) "Feil ved oppslag på ident $ident. " else "Feil ved oppslag på person."
        secureLogger.error(
            errorMelding +
                "PDL rapporterte ingen feil men returnerte tomt datafelt",
        )
        throw PdlRequestException("Manglende ${RESULT::class} ved feilfri respons fra PDL. Se secure logg for detaljer.")
    }
    return data
}

inline fun <reified RESULT : Any> feilsjekkOgReturnerData(pdlResponse: PdlBolkResponse<RESULT>): Map<String, RESULT> {
    if (pdlResponse.data == null) {
        secureLogger.error("Data fra pdl er null ved bolkoppslag av ${RESULT::class} fra PDL: ${pdlResponse.errorMessages()}")
        throw PdlRequestException("Data er null fra PDL -  ${RESULT::class}. Se secure logg for detaljer.")
    }

    val feil = pdlResponse.data.personBolk.filter { it.code != "ok" }.associate { it.ident to it.code }
    if (feil.isNotEmpty()) {
        secureLogger.error("Feil ved henting av ${RESULT::class} fra PDL: $feil")
        throw PdlRequestException("Feil ved henting av ${RESULT::class} fra PDL. Se secure logg for detaljer.")
    }
    if (pdlResponse.harAdvarsel()) {
        logger.warn("Advarsel ved henting av ${RESULT::class} fra PDL. Se securelogs for detaljer.")
        secureLogger.warn("Advarsel ved henting av ${RESULT::class} fra PDL: ${pdlResponse.extensions?.warnings}")
    }
    return pdlResponse.data.personBolk.associateBy({ it.ident }, { it.person!! })
}
