package no.nav.tilleggsstonader.soknad.soknad.domene

import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
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
    val type: Stønadstype,
    val count: Long,
)

@Table("skjema")
data class Skjema(
    @Id
    val id: UUID = UUID.randomUUID(),
    val opprettetTid: LocalDateTime = SporbarUtils.now(),
    val type: Stønadstype,
    val personIdent: String,
    @Column("skjema_json")
    val skjemaJson: JsonWrapper,
    @Column("skjema_pdf")
    val skjemaPdf: ByteArray? = null,
    val journalpostId: String? = null,
    @Version
    val version: Int = 0,
    @Column("frontend_git_hash")
    val frontendGitHash: String?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Skjema

        if (id != other.id) return false
        if (opprettetTid != other.opprettetTid) return false
        if (type != other.type) return false
        if (personIdent != other.personIdent) return false
        if (skjemaJson != other.skjemaJson) return false
        if (skjemaPdf != null) {
            if (other.skjemaPdf == null) return false
            if (!skjemaPdf.contentEquals(other.skjemaPdf)) return false
        } else if (other.skjemaPdf != null) {
            return false
        }
        if (journalpostId != other.journalpostId) return false
        return version == other.version
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + opprettetTid.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + personIdent.hashCode()
        result = 31 * result + skjemaJson.hashCode()
        result = 31 * result + (skjemaPdf?.contentHashCode() ?: 0)
        result = 31 * result + (journalpostId?.hashCode() ?: 0)
        result = 31 * result + version
        return result
    }
}
