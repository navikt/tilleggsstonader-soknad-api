package no.nav.tilleggsstonader.soknad

import org.springframework.boot.builder.SpringApplicationBuilder

fun main(args: Array<String>) {
    SpringApplicationBuilder(App::class.java)
        .profiles("local")
        .run(*args)
}
