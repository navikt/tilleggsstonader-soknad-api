package no.nav.tilleggsstonader.soknad.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FileUtilTest {

    @Test
    fun `skal ikke sjekke inn SKAL_SKRIVE_TIL_FIL=true`() {
        assertThat(FileUtil.SKAL_SKRIVE_TIL_FIL).isFalse()
    }
}
