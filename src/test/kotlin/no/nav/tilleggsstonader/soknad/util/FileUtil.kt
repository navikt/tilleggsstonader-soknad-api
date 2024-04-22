package no.nav.tilleggsstonader.soknad.util

import no.nav.tilleggsstonader.kontrakter.felles.ObjectMapperProvider.objectMapper
import java.io.File

object FileUtil {
    fun readFile(filnavn: String): String = FileUtil::class.java.classLoader.getResource(filnavn)!!.readText()
    fun readBytes(filnavn: String): ByteArray = FileUtil::class.java.classLoader.getResource(filnavn)!!.readBytes()

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
        if (!SKAL_SKRIVE_TIL_FIL) {
            return
        }
        val file = File("src/test/resources/$navn")
        if (!file.exists()) {
            file.createNewFile()
        }
        file.writeBytes(bytes)
    }

    /**
     * Denne kan endres hvis man ønsker å skrive over filer som brukes i tester, eks:
     * 1. Tester feiler pga endringer
     * 2. Setter denne til true
     * 3. Kjører tester på nytt, 2 ganger, 1 gang for å skrive filen, en andre gång for å verifisere
     * 4. set denne til false på nytt, hvis ikke feiler [FileUtilTest]
     */
    const val SKAL_SKRIVE_TIL_FIL = true
}
