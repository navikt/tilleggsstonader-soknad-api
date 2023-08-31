package no.nav.tilleggsstonader.soknad.util

object FileUtil {
    fun readFile(filnavn: String): String = FileUtil::class.java.classLoader.getResource(filnavn)!!.readText()
}
