package no.nav.tilleggsstonader.soknad.infrastruktur.jackson
import no.nav.tilleggsstonader.kontrakter.felles.ObjectMapperProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JacksonConfig {
    @Bean
    fun jsonMapper() = ObjectMapperProvider.jsonMapper
}
