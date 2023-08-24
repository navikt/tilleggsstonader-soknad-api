package no.nav.tilleggsstonader.soknad

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(
    exclude = [ErrorMvcAutoConfiguration::class]
)
class App

fun main(args: Array<String>) {
    runApplication<App>(*args)
}
