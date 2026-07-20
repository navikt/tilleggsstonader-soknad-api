package no.nav.tilleggsstonader.soknad.soknad.passAvBarn

import no.nav.tilleggsstonader.kontrakter.søknad.felles.ArbeidOgOppholdAvsnitt
import no.nav.tilleggsstonader.kontrakter.søknad.felles.OppholdUtenforNorge
import no.nav.tilleggsstonader.soknad.soknad.ArbeidOgOppholdDto
import no.nav.tilleggsstonader.soknad.soknad.OppholdUtenforNorgeDto

object ArbeidOgOppholdMapper {
    fun mapArbeidOgOpphold(arbeidOgOpphold: ArbeidOgOppholdDto): ArbeidOgOppholdAvsnitt? {
        if (listOfNotNull(
                arbeidOgOpphold.jobberIAnnetLand,
                arbeidOgOpphold.harPengestøtteAnnetLand,
                arbeidOgOpphold.harOppholdUtenforNorgeSiste12mnd,
            ).isEmpty()
        ) {
            return null
        }
        return ArbeidOgOppholdAvsnitt(
            jobberIAnnetLand = arbeidOgOpphold.jobberIAnnetLand,
            jobbAnnetLand = arbeidOgOpphold.jobbAnnetLand,
            harPengestøtteAnnetLand = arbeidOgOpphold.harPengestøtteAnnetLand,
            pengestøtteAnnetLand = arbeidOgOpphold.pengestøtteAnnetLand,
            harOppholdUtenforNorgeSiste12mnd = arbeidOgOpphold.harOppholdUtenforNorgeSiste12mnd,
            oppholdUtenforNorgeSiste12mnd = mapOpphold(arbeidOgOpphold.oppholdUtenforNorgeSiste12mnd),
            harOppholdUtenforNorgeNeste12mnd = arbeidOgOpphold.harOppholdUtenforNorgeNeste12mnd,
            oppholdUtenforNorgeNeste12mnd = mapOpphold(arbeidOgOpphold.oppholdUtenforNorgeNeste12mnd),
        )
    }

    private fun mapOpphold(liste: List<OppholdUtenforNorgeDto>): List<OppholdUtenforNorge> =
        liste.map {
            OppholdUtenforNorge(
                land = it.land,
                årsak = it.årsak,
                fom = it.fom,
                tom = it.tom,
            )
        }
}
