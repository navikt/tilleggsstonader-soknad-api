package no.nav.tilleggsstonader.soknad.soknad.domene

import no.nav.tilleggsstonader.kontrakter.søknad.Vedleggstype
import no.nav.tilleggsstonader.soknad.infrastruktur.database.SporbarUtils
import no.nav.tilleggsstonader.soknad.infrastruktur.database.repository.InsertUpdateRepository
import no.nav.tilleggsstonader.soknad.infrastruktur.database.repository.RepositoryInterface
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface VedleggRepository : RepositoryInterface<Vedlegg, UUID>, InsertUpdateRepository<Vedlegg> {
    fun findBySøknadId(søknadId: UUID): List<Vedlegg>
}

class Vedlegg(
    @Id
    val id: UUID,
    @Column("soknad_id")
    val søknadId: UUID,
    val type: Vedleggstype,
    val navn: String,
    val innhold: ByteArray,
    val opprettetTid: LocalDateTime = SporbarUtils.now(),
) {

    /**
     * Pga Bytearray må vi legge inn equals/hashcode
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vedlegg

        if (id != other.id) return false
        if (søknadId != other.søknadId) return false
        if (type != other.type) return false
        if (navn != other.navn) return false
        if (!innhold.contentEquals(other.innhold)) return false
        return opprettetTid == other.opprettetTid
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + søknadId.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + navn.hashCode()
        result = 31 * result + innhold.contentHashCode()
        result = 31 * result + opprettetTid.hashCode()
        return result
    }
}
