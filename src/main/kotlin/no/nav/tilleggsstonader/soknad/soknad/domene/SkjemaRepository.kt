package no.nav.tilleggsstonader.soknad.soknad.domene

import no.nav.tilleggsstonader.kontrakter.felles.Skjematype
import no.nav.tilleggsstonader.soknad.infrastruktur.database.ByteArrayWrapper
import no.nav.tilleggsstonader.soknad.infrastruktur.database.JsonWrapper
import no.nav.tilleggsstonader.soknad.infrastruktur.database.SporbarUtils
import no.nav.tilleggsstonader.soknad.infrastruktur.database.repository.InsertUpdateRepository
import no.nav.tilleggsstonader.soknad.infrastruktur.database.repository.RepositoryInterface
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface SkjemaRepository :
    RepositoryInterface<Skjema, UUID>,
    InsertUpdateRepository<Skjema> {
    @Query("SELECT type, count(*) as count FROM skjema GROUP BY type")
    fun finnAntallPerType(): List<AntallPerType>
}

data class AntallPerType(
    val type: Skjematype,
    val count: Long,
)

@Table("skjema")
data class Skjema(
    @Id
    val id: UUID = UUID.randomUUID(),
    val opprettetTid: LocalDateTime = SporbarUtils.now(),
    val type: Skjematype,
    val personIdent: String,
    @Column("skjema_json")
    val skjemaJson: JsonWrapper,
    @Column("skjema_pdf")
    val skjemaPdf: ByteArrayWrapper? = null,
    val journalpostId: String? = null,
    @Version
    val version: Int = 0,
    @Column("frontend_git_hash")
    val frontendGitHash: String?,
)
