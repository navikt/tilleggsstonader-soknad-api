package no.nav.tilleggsstonader.soknad

import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskLogg
import no.nav.familie.prosessering.internal.TaskService
import no.nav.familie.prosessering.internal.TaskWorker
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.tilleggsstonader.libs.test.fnr.FnrGenerator
import no.nav.tilleggsstonader.soknad.infrastruktur.IntegrasjonerClient
import no.nav.tilleggsstonader.soknad.infrastruktur.IntegrasjonerClientConfig.Companion.resetIntegrasjonerClientMock
import no.nav.tilleggsstonader.soknad.infrastruktur.PdlClientConfig.Companion.resetPdlClientMock
import no.nav.tilleggsstonader.soknad.person.pdl.PdlClient
import no.nav.tilleggsstonader.soknad.soknad.domene.Skjema
import no.nav.tilleggsstonader.soknad.soknad.domene.Vedlegg
import no.nav.tilleggsstonader.soknad.util.DbContainerInitializer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.data.jdbc.core.JdbcAggregateOperations
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.UUID

val tokenSubject = "12345678911"

@ExtendWith(SpringExtension::class)
@ContextConfiguration(initializers = [DbContainerInitializer::class])
@SpringBootTest(classes = [App::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(
    "integrasjonstest",
    "mock-pdl",
    "mock-sak",
    "mock-vedlegg",
    "mock-kodeverk",
    "mock-aktivitet",
    "mock-htmlify",
    "mock-dokument",
    "mock-integrasjoner",
    "mock-kafka",
)
@EnableMockOAuth2Server
@AutoConfigureWebTestClient
abstract class IntegrationTest {
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private lateinit var mockOAuth2Server: MockOAuth2Server

    @Autowired
    protected lateinit var pdlClient: PdlClient

    @Autowired
    private lateinit var jdbcAggregateOperations: JdbcAggregateOperations

    @Autowired
    private lateinit var cacheManagers: List<CacheManager>

    @Autowired
    lateinit var taskService: TaskService

    @Autowired
    lateinit var taskWorker: TaskWorker

    @Autowired
    lateinit var integrasjonerClient: IntegrasjonerClient

    @Autowired
    lateinit var webTestClient: WebTestClient

    val logger = LoggerFactory.getLogger(javaClass)

    @AfterEach
    fun tearDown() {
        clearClientMocks()
        resetDatabase()
        clearCaches()
    }

    private fun resetDatabase() {
        listOf(
            TaskLogg::class,
            Task::class,
            Vedlegg::class,
            Skjema::class,
        ).forEach { jdbcAggregateOperations.deleteAll(it.java) }
    }

    private fun clearClientMocks() {
        resetPdlClientMock(pdlClient)
        resetIntegrasjonerClientMock(integrasjonerClient)
    }

    private fun clearCaches() {
        cacheManagers.forEach {
            it.cacheNames
                .mapNotNull { cacheName -> it.getCache(cacheName) }
                .forEach { cache -> cache.clear() }
        }
    }

    protected fun søkerBearerToken(personident: String = FnrGenerator.generer()): String = mockOAuth2Server.token(subject = personident)

    fun WebTestClient.RequestHeadersSpec<*>.medSøkerBearerToken(personident: String = FnrGenerator.generer()) =
        this.headers {
            it.setBearerAuth(søkerBearerToken(personident))
        }

    private fun MockOAuth2Server.token(
        subject: String,
        issuerId: String = "tokenx",
        clientId: String = UUID.randomUUID().toString(),
        audience: String = "tilleggsstonader-app",
        claims: Map<String, Any> =
            mapOf(
                "acr" to "Level4",
                "pid" to subject,
            ),
    ): String =
        this
            .issueToken(
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
