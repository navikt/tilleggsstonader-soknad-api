package no.nav.tilleggsstonader.soknad.person.pdl

import no.nav.tilleggsstonader.soknad.person.pdl.dto.Adressebeskyttelse
import no.nav.tilleggsstonader.soknad.person.pdl.dto.AdressebeskyttelseGradering
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PdlUtilKtTest {

    @Nested
    inner class FortroligEllerStrengtFortrolig {

        @Test
        fun `graderinger utenom ugradert er fortrolig eller strengt fortrolig`() {
            AdressebeskyttelseGradering.entries.filter { it != AdressebeskyttelseGradering.UGRADERT }.forEach {
                assertThat(listOf(Adressebeskyttelse(it)).fortroligEllerStrengtFortrolig()).isTrue()
            }
        }

        @Test
        fun `flere graderinger utenom ugradert er fortrolig eller strengt fortrolig`() {
            val adressebeskyttelse = listOf(
                Adressebeskyttelse(AdressebeskyttelseGradering.UGRADERT),
                Adressebeskyttelse(AdressebeskyttelseGradering.FORTROLIG),
            )
            assertThat(adressebeskyttelse.fortroligEllerStrengtFortrolig()).isTrue()
        }

        @Test
        fun `ugradert er ikke fortrolig eller strengt fortrolig`() {
            val adressebeskyttelse = listOf(Adressebeskyttelse(AdressebeskyttelseGradering.UGRADERT))
            assertThat(adressebeskyttelse.fortroligEllerStrengtFortrolig()).isFalse()
        }

        @Test
        fun `mangler verdi er ikke fortrolig eller strengt fortrolig`() {
            assertThat(listOf<Adressebeskyttelse>().fortroligEllerStrengtFortrolig()).isFalse()
        }
    }
}
