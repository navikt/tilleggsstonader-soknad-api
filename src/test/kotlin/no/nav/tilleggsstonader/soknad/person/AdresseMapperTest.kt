package no.nav.tilleggsstonader.soknad.person

import io.mockk.every
import io.mockk.mockk
import no.nav.tilleggsstonader.soknad.infrastruktur.lagPdlSøker
import no.nav.tilleggsstonader.soknad.kodeverk.KodeverkService
import no.nav.tilleggsstonader.soknad.person.pdl.dto.Bostedsadresse
import no.nav.tilleggsstonader.soknad.person.pdl.dto.Matrikkeladresse
import no.nav.tilleggsstonader.soknad.person.pdl.dto.Vegadresse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AdresseMapperTest {
    val kodeverkService = mockk<KodeverkService>()

    val mapper = AdresseMapper(kodeverkService)

    @BeforeEach
    fun setUp() {
        every { kodeverkService.hentPoststed(any()) } answers { if (firstArg<String?>() == null) "" else "Oslo" }
    }

    @Test
    fun `skal mappe alle felter fra vegadresse`() {
        val vegadresse =
            Vegadresse(
                husnummer = "1",
                husbokstav = "A",
                bruksenhetsnummer = "0101",
                adressenavn = "Min vei",
                postnummer = "0010",
            )
        val pdlSøker = lagPdlSøker(bostedsadresse = Bostedsadresse(vegadresse, matrikkeladresse = null))

        val adresse = mapper.tilFormatertAdresse(pdlSøker)

        assertThat(adresse).isEqualTo("Min vei 1 A 0101, 0010 Oslo")
    }

    @Test
    fun `skal mappe til tom streng hvis alle felter mangler på vegadresse`() {
        val vegadresse =
            Vegadresse(
                husnummer = null,
                husbokstav = null,
                bruksenhetsnummer = null,
                adressenavn = null,
                postnummer = null,
            )

        val pdlSøker = lagPdlSøker(bostedsadresse = Bostedsadresse(vegadresse, matrikkeladresse = null))

        val adresse = mapper.tilFormatertAdresse(pdlSøker)

        assertThat(adresse).isEqualTo("")
    }

    @Test
    fun `skal mappe matrikkeladresse hvis vegadresse mangler`() {
        val matrikkeladresse = Matrikkeladresse("Tilleggsnavn", "0010")

        val pdlSøker = lagPdlSøker(bostedsadresse = Bostedsadresse(null, matrikkeladresse))

        val adresse = mapper.tilFormatertAdresse(pdlSøker)

        assertThat(adresse).isEqualTo("Tilleggsnavn, 0010 Oslo")
    }
}
