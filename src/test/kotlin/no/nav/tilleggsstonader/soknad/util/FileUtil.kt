package no.nav.tilleggsstonader.soknad.util

import no.nav.tilleggsstonader.kontrakter.felles.ObjectMapperProvider.objectMapper
import java.io.File

object FileUtil {
    fun readFile(filnavn: String): String = FileUtil::class.java.classLoader.getResource(filnavn)!!.readText()

    @Suppress("unused")
    fun skrivJsonTilFil(navn: String, data: Any) {
        skrivTilFil(navn, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data))
    }

    @Suppress("unused")
    fun skrivTilFil(navn: String, data: String) {
        skrivTilFil(navn, data.toByteArray())
    }

    @Suppress("unused")
    fun skrivTilFil(navn: String, bytes: ByteArray) {
        val file = File("src/test/resources/$navn")
        if (!file.exists()) {
            file.createNewFile()
        }
        file.writeBytes(bytes)
    }
}
