package no.nav.tilleggsstonader.soknad.util

import java.io.File

object FileUtil {
    fun readFile(filnavn: String): String = FileUtil::class.java.classLoader.getResource(filnavn)!!.readText()

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
