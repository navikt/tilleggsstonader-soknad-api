package no.nav.tilleggsstonader.soknad

import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskLogg
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.tilleggsstonader.libs.test.fnr.FnrGenerator
import no.nav.tilleggsstonader.soknad.infrastruktur.PdlClientConfig.Companion.resetPdlClientMock
import no.nav.tilleggsstonader.soknad.person.pdl.PdlClient
import no.nav.tilleggsstonader.soknad.soknad.domene.Søknad
import no.nav.tilleggsstonader.soknad.soknad.domene.Vedlegg
import no.nav.tilleggsstonader.soknad.util.DbContainerInitializer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jdbc.core.JdbcAggregateOperations
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.client.RestTemplate
import java.util.UUID

val tokenSubject = "12345678911"

// Slett denne når RestTemplateConfiguration er tatt i bruk?
@Configuration
class DefaultRestTemplateConfiguration {

    @Bean
    fun restTemplate(restTemplateBuilder: RestTemplateBuilder) =
        restTemplateBuilder.build()
}

@ExtendWith(SpringExtension::class)
@ContextConfiguration(initializers = [DbContainerInitializer::class])
@SpringBootTest(classes = [App::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(
    "integrasjonstest",
    "mock-pdl",
)
@EnableMockOAuth2Server
abstract class IntegrationTest {

    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    protected lateinit var restTemplate: RestTemplate
    protected val headers = HttpHeaders()

    @LocalServerPort
    private var port: Int? = 0

    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private lateinit var mockOAuth2Server: MockOAuth2Server

    @Autowired
    protected lateinit var pdlClient: PdlClient

    @Autowired
    private lateinit var jdbcAggregateOperations: JdbcAggregateOperations

    @AfterEach
    fun tearDown() {
        headers.clear()
        clearClientMocks()
        resetDatabase()
    }

    private fun resetDatabase() {
        listOf(
            TaskLogg::class,
            Task::class,

            Vedlegg::class,
            Søknad::class,
        ).forEach { jdbcAggregateOperations.deleteAll(it.java) }
    }

    private fun clearClientMocks() {
        resetPdlClientMock(pdlClient)
    }

    protected fun localhost(path: String): String {
        return "$LOCALHOST$port/$path"
    }

    protected fun søkerBearerToken(personident: String = FnrGenerator.generer()): String {
        return mockOAuth2Server.token(subject = personident)
    }

    private fun MockOAuth2Server.token(
        subject: String,
        issuerId: String = "tokenx",
        clientId: String = UUID.randomUUID().toString(),
        audience: String = "tilleggsstonader-app",
        claims: Map<String, Any> = mapOf(
            "acr" to "Level4",
            "pid" to subject,
        ),
    ): String {
        return this.issueToken(
            issuerId,
            clientId,
            DefaultOAuth2TokenCallback(
                issuerId = issuerId,
                subject = subject,
                audience = listOf(audience),
                claims = claims,
                expiry = 3600,
            ),
        ).serialize()
    }

    companion object {
        private const val LOCALHOST = "http://localhost:"
    }
}
