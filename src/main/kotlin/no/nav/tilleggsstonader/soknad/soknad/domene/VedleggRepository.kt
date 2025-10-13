package no.nav.tilleggsstonader.soknad.soknad.domene

import no.nav.tilleggsstonader.kontrakter.søknad.Vedleggstype
import no.nav.tilleggsstonader.soknad.infrastruktur.database.ByteArrayWrapper
import no.nav.tilleggsstonader.soknad.infrastruktur.database.SporbarUtils
import no.nav.tilleggsstonader.soknad.infrastruktur.database.repository.InsertUpdateRepository
import no.nav.tilleggsstonader.soknad.infrastruktur.database.repository.RepositoryInterface
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface VedleggRepository :
    RepositoryInterface<Vedlegg, UUID>,
    InsertUpdateRepository<Vedlegg> {
    fun findBySkjemaId(skjemaId: UUID): List<Vedlegg>
}

data class Vedlegg(
    @Id
    val id: UUID,
    @Column("skjema_id")
    val skjemaId: UUID,
    val type: Vedleggstype,
    val navn: String,
    val innhold: ByteArrayWrapper,
    val opprettetTid: LocalDateTime = SporbarUtils.now(),
)
