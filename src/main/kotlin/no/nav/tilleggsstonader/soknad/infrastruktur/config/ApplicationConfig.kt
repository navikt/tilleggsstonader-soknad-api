package no.nav.tilleggsstonader.soknad.infrastruktur.config

import no.nav.tilleggsstonader.soknad.infrastruktur.config.SecureLogger.secureLogger
import org.springframework.boot.SpringBootConfiguration

@SpringBootConfiguration
class ApplicationConfig {

    init {
        secureLogger.info("securelogger test")
    }
}
