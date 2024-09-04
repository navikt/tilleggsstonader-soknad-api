package no.nav.tilleggsstonader.soknad.soknad

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.prosessering.internal.TaskService
import no.nav.tilleggsstonader.kontrakter.søknad.Dokument
import no.nav.tilleggsstonader.kontrakter.søknad.DokumentasjonFelt
import no.nav.tilleggsstonader.kontrakter.søknad.Vedleggstype
import no.nav.tilleggsstonader.libs.test.fnr.FnrGenerator
import no.nav.tilleggsstonader.libs.utils.fnr.Fødselsnummer
import no.nav.tilleggsstonader.libs.utils.osloDateNow
import no.nav.tilleggsstonader.libs.utils.osloNow
import no.nav.tilleggsstonader.soknad.dokument.FamilieVedleggClient
import no.nav.tilleggsstonader.soknad.person.PersonService
import no.nav.tilleggsstonader.soknad.person.dto.Barn
import no.nav.tilleggsstonader.soknad.person.dto.PersonMedBarnDto
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.BarnetilsynMapper
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.SøknadBarnetilsynUtil
import no.nav.tilleggsstonader.soknad.soknad.domene.SøknadRepository
import no.nav.tilleggsstonader.soknad.soknad.domene.VedleggRepository
import no.nav.tilleggsstonader.soknad.soknad.laeremidler.LæremidlerMapper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID
import no.nav.tilleggsstonader.soknad.soknad.domene.Vedlegg as VedleggDomene

class SøknadServiceTest {

    private val søknadRepository = mockk<SøknadRepository>()
    private val vedleggRepository = mockk<VedleggRepository>()
    private val personService = mockk<PersonService>()
    private val familieVedleggClient = mockk<FamilieVedleggClient>()

    private val service = SøknadService(
        søknadRepository = søknadRepository,
        vedleggRepository = vedleggRepository,
        barnetilsynMapper = BarnetilsynMapper(),
        læremidlerMapper = LæremidlerMapper(),
        taskService = mockk<TaskService>(relaxed = true),
        personService = personService,
        familieVedleggClient = familieVedleggClient,
    )

    val personIdent = FnrGenerator.generer()
    val søknad = SøknadBarnetilsynUtil.søknad
    val person = mockk<PersonMedBarnDto>()

    val vedleggSlot = slot<List<VedleggDomene>>()

    @BeforeEach
    fun setUp() {
        every { søknadRepository.insert(any()) } answers { firstArg() }
        every { person.barn } returns søknad.barnMedBarnepass.map {
            Barn(it.ident, "fornavn", "fornavn etternavn", osloDateNow(), 3)
        }
        every { personService.hentSøker(Fødselsnummer(personIdent)) } returns person
        every { vedleggRepository.insertAll(capture(vedleggSlot)) } answers { firstArg() }
    }

    @Test
    fun `skal kaste feil hvis man prøver å sende inn barn som ikke finnes på søker`() {
        every { person.barn } returns emptyList()

        assertThatThrownBy {
            service.lagreSøknad(personIdent, osloNow(), søknad)
        }.hasMessageContaining("Prøver å sende inn identer på barnen")
    }

    @Nested
    inner class Vedlegg {
        @Test
        fun `skal lagre innsendte vedlegg`() {
            every { familieVedleggClient.hentVedlegg(any()) } returns byteArrayOf(12)
            val vedlegg = Dokument(UUID.randomUUID(), UUID.randomUUID().toString())

            val dokumentasjon = lagDokumentasjonFelt(vedlegg)
            val søknadId =
                service.lagreSøknad(personIdent, osloNow(), søknad.copy(dokumentasjon = dokumentasjon))

            val lagretVedlegg = vedleggSlot.captured.single()
            assertThat(lagretVedlegg.id).isEqualTo(vedlegg.id)
            assertThat(lagretVedlegg.type).isEqualTo(Vedleggstype.UTGIFTER_PASS_SFO_AKS_BARNEHAGE)
            assertThat(lagretVedlegg.søknadId).isEqualTo(søknadId)
            assertThat(lagretVedlegg.navn).isEqualTo(vedlegg.navn)
            assertThat(lagretVedlegg.innhold).isEqualTo(byteArrayOf(12))
        }

        @Test
        fun `skal feile hvis man ikke får hentet vedlegget`() {
            every { familieVedleggClient.hentVedlegg(any()) } throws RuntimeException("feilet")
            val vedlegg = Dokument(UUID.randomUUID(), "navn")

            val dokumentasjon = lagDokumentasjonFelt(vedlegg)
            assertThatThrownBy {
                service.lagreSøknad(personIdent, osloNow(), søknad.copy(dokumentasjon = dokumentasjon))
            }.hasMessageContaining("Feilet henting av vedlegg=${vedlegg.id}")
        }
    }

    private fun lagDokumentasjonFelt(vedlegg: Dokument) = listOf(
        DokumentasjonFelt(
            type = Vedleggstype.UTGIFTER_PASS_SFO_AKS_BARNEHAGE,
            label = "label",
            opplastedeVedlegg = listOf(vedlegg),
        ),
    )
}
