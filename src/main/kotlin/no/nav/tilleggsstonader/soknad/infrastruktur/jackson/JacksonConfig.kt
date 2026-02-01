package no.nav.tilleggsstonader.soknad.infrastruktur.jackson

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse
import no.nav.tilleggsstonader.kontrakter.felles.JsonMapperProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.addMixIn

@Configuration
class JacksonConfig {
    @Bean
    fun jsonMapper(): JsonMapper =
        JsonMapperProvider.jsonMapper
            .rebuild()
            .addMixIn<OAuth2AccessTokenResponse, OAuth2AccessTokenResponseMixin>()
            .build()
}

abstract class OAuth2AccessTokenResponseMixin {
    @JsonIgnore
    @Suppress("ktlint:standard:function-naming")
    abstract fun getAccess_token(): String

    @JsonIgnore
    @Suppress("ktlint:standard:function-naming")
    abstract fun getExpires_at(): Long

    @JsonIgnore
    @Suppress("ktlint:standard:function-naming")
    abstract fun getExpires_in(): Int

    @JsonProperty("access_token")
    abstract fun getAccessToken(): String

    @JsonProperty("expires_at")
    abstract fun getExpiresAt(): Long

    @JsonProperty("expires_in")
    abstract fun getExpiresIn(): Int
}
