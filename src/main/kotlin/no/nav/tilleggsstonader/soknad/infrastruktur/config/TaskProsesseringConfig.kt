package no.nav.tilleggsstonader.soknad.infrastruktur.config

import no.nav.familie.prosessering.config.ProsesseringInfoProvider
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@ComponentScan("no.nav.familie.prosessering")
@Configuration
class TaskProsesseringConfig(
    @Value("\${prosessering.rolle}") private val rolle: String,
) {

    @Bean
    fun prosesseringInfoProvider() = object : ProsesseringInfoProvider {
        override fun hentBrukernavn(): String = try {
            SpringTokenValidationContextHolder().tokenValidationContext.getClaims("azuread")
                .getStringClaim("preferred_username")
        } catch (e: Exception) {
            error("Mangler preferred_username på request")
        }

        override fun harTilgang(): Boolean {
            return hentGrupperFraToken().contains(rolle)
        }
    }

    fun hentGrupperFraToken(): Set<String> = try {
        SpringTokenValidationContextHolder().tokenValidationContext
            .getClaims("azuread")
            ?.get("groups") as List<String>?
    } catch (e: Exception) {
        error("Mangler groups på request")
    }?.toSet() ?: emptySet()
}
