package no.nav.tilleggsstonader.soknad.soknad.domene

import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.soknad.infrastruktur.database.JsonWrapper
import no.nav.tilleggsstonader.soknad.infrastruktur.database.SporbarUtils
import no.nav.tilleggsstonader.soknad.infrastruktur.database.repository.InsertUpdateRepository
import no.nav.tilleggsstonader.soknad.infrastruktur.database.repository.RepositoryInterface
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface SøknadRepository : RepositoryInterface<Søknad, UUID>, InsertUpdateRepository<Søknad>

@Table("soknad")
data class Søknad(
    @Id
    val id: UUID = UUID.randomUUID(),
    val opprettetTid: LocalDateTime = SporbarUtils.now(),
    val type: Stønadstype,
    val personIdent: String,
    @Column("soknad_json")
    val søknadJson: JsonWrapper,

    @Column("soknad_pdf")
    val søknadPdf: ByteArray? = null,

    val journalpostId: String? = null,

    @Version
    val version: Int = 0, // TODO lag test
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Søknad

        if (id != other.id) return false
        if (opprettetTid != other.opprettetTid) return false
        if (type != other.type) return false
        if (personIdent != other.personIdent) return false
        if (søknadJson != other.søknadJson) return false
        if (!søknadPdf.contentEquals(other.søknadPdf)) return false
        return journalpostId == other.journalpostId
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + opprettetTid.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + personIdent.hashCode()
        result = 31 * result + søknadJson.hashCode()
        result = 31 * result + søknadPdf.contentHashCode()
        result = 31 * result + (journalpostId?.hashCode() ?: 0)
        return result
    }
}
