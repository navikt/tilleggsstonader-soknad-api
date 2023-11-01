package no.nav.tilleggsstonader.soknad.soknad

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tilleggsstonader.kontrakter.felles.Hovedytelse
import no.nav.tilleggsstonader.kontrakter.søknad.EnumFelt
import no.nav.tilleggsstonader.kontrakter.søknad.JaNei
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.TypeBarnepass
import no.nav.tilleggsstonader.kontrakter.søknad.barnetilsyn.ÅrsakBarnepass
import no.nav.tilleggsstonader.libs.sikkerhet.EksternBrukerUtils
import no.nav.tilleggsstonader.libs.utils.fnr.Fødselsnummer
import no.nav.tilleggsstonader.soknad.person.PersonService
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.Aktivitet
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.BarnMedBarnepass
import no.nav.tilleggsstonader.soknad.soknad.barnetilsyn.SøknadBarnetilsynDto
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("api/soknad")
@ProtectedWithClaims(issuer = EksternBrukerUtils.ISSUER_TOKENX, claimMap = ["acr=Level4"])
@Validated
class SøknadController(
    private val søknadService: SøknadService,
    private val personService: PersonService,
) {

    @PostMapping("barnetilsyn")
    fun sendInn(@RequestBody søknad: SøknadBarnetilsynDto): Kvittering {
        val mottattTidspunkt = LocalDateTime.now()
        søknadService.lagreSøknad(
            personIdent = EksternBrukerUtils.hentFnrFraToken(),
            mottattTidspunkt = mottattTidspunkt,
            søknad = søknad,
        )
        return Kvittering(mottattTidspunkt = mottattTidspunkt)
    }

    @PostMapping("test")
    fun sendInn(): Kvittering {
        val mottattTidspunkt = LocalDateTime.now()
        val barn = personService.hentSøker(Fødselsnummer(EksternBrukerUtils.hentFnrFraToken())).barn.map {
            BarnMedBarnepass(
                ident = it,
                type = EnumFelt("Type barnepass", TypeBarnepass.BARNEHAGE_SFO_AKS, "Svartekst", listOf("type1", "type2")),
                startetIFemte = EnumFelt("Har startet i 5. klasse?", JaNei.JA, "Ja", listOf("Ja", "Nei")),
                årsak = EnumFelt("Årsak?", ÅrsakBarnepass.MYE_BORTE_ELLER_UVANLIG_ARBEIDSTID, "Mye borte", emptyList()),
            )
        }
        søknadService.lagreSøknad(
            personIdent = EksternBrukerUtils.hentFnrFraToken(),
            mottattTidspunkt = mottattTidspunkt,
            søknad = SøknadBarnetilsynDto(
                hovedytelse = EnumFelt("Hovedutelse?", Hovedytelse.AAP, "AAP", listOf("AAP", "Overgangsstønad")),
                aktivitet = Aktivitet(
                    utdanning = EnumFelt(
                        "Skal du søke om støtte til pass av barn i forbindelse med denne utdanningen?",
                        JaNei.JA,
                        "Ja",
                        listOf("Ja", "Nei"),
                    ),
                ),
                barn = barn,
            ),
        )
        return Kvittering(mottattTidspunkt = mottattTidspunkt)
    }
}
