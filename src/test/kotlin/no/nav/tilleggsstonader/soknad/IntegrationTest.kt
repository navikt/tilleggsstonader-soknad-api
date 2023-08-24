package no.nav.tilleggsstonader.soknad

import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [App::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class IntegrationTest {

    protected val restTemplate = TestRestTemplate()

    @LocalServerPort
    private var port: Int? = 0

    protected fun localhost(path: String): String {
        return "$LOCALHOST$port/$path"
    }

    companion object {
        private const val LOCALHOST = "http://localhost:"
    }
}
