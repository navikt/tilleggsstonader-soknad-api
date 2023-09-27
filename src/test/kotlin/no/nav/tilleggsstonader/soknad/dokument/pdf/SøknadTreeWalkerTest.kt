package no.nav.tilleggsstonader.soknad.dokument.pdf

import no.nav.tilleggsstonader.kontrakter.felles.ObjectMapperProvider.objectMapper
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.BarnetilsynMapper
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.SøknadBarnetilsynUtil
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SøknadTreeWalkerTest {

    @Nested
    inner class Barnetilsyn {
        @Test
        fun `skal mappe barnetilsyn`() {
            val result = SøknadTreeWalker.mapSøknad(BarnetilsynMapper().map(SøknadBarnetilsynUtil.søknad), emptyList())
            println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result))
        }
    }
}
