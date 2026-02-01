package no.nav.tilleggsstonader.soknad

import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import tools.jackson.databind.json.JsonMapper

@SpringBootTest(classes = [no.nav.tilleggsstonader.soknad.infrastruktur.jackson.JacksonConfig::class])
class OAuth2AccessTokenResponseTest {
    @Autowired
    private lateinit var objectMapper: JsonMapper

    @Test
    fun `skal deserialisere OAuth2AccessTokenResponse med Spring-konfigurert ObjectMapper`() {
        val json =
            """
            {
              "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.example",
              "expires_at": 1738156800,
              "expires_in": 3600,
              "additionalParameters": {
                "token_type": "Bearer",
                "scope": "openid profile email"
              }
            }
            """.trimIndent()

        // Testen verifiserer at JacksonConfig med mixin løser getter-konfliktene
        // slik at deserialisering ikke kaster InvalidDefinitionException
        val response =
            assertDoesNotThrow {
                objectMapper.readValue(json, OAuth2AccessTokenResponse::class.java)
            }

        assertThat(response.access_token).isEqualTo("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.example")
        assertThat(response.expires_at).isEqualTo(1738156800)
        assertThat(response.expires_in).isEqualTo(3600)
    }
}
