package no.nav.tilleggsstonader.soknad.util

import no.nav.tilleggsstonader.soknad.util.CollectionUtil.singleOrNullOrError
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CollectionUtilTest {
    @Nested
    inner class SingleOrNullOrError {
        @Test
        fun `under 2 element skal hente elementet eller null`() {
            assertThat(listOf(1).singleOrNullOrError()).isEqualTo(1)
            assertThat(listOf<Int>().singleOrNullOrError()).isNull()
        }

        @Test
        fun `fler enn 1 element skal kaste feil`() {
            assertThatThrownBy {
                listOf(1, 2).singleOrNullOrError()
            }.hasMessage("Forventet ikke fler enn 1 element av typen Int")
        }
    }
}
