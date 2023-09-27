package no.nav.tilleggsstonader.soknad.soknad

import no.nav.tilleggsstonader.soknad.infrastruktur.database.repository.InsertUpdateRepository
import no.nav.tilleggsstonader.soknad.infrastruktur.database.repository.RepositoryInterface
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.stereotype.Repository
import java.util.Objects
import java.util.UUID

@Repository
interface VedleggRepository : RepositoryInterface<Søknad, UUID>, InsertUpdateRepository<Søknad>

class Vedlegg(
    @Id
    val id: UUID,
    @Column("soknad_id")
    val søknadId: UUID,
    val navn: String,
    val tittel: String,
    val innhold: ByteArray,
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
        if (navn != other.navn) return false
        if (tittel != other.tittel) return false
        return innhold.contentEquals(other.innhold)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + søknadId.hashCode()
        result = 31 * result + navn.hashCode()
        result = 31 * result + tittel.hashCode()
        result = 31 * result + innhold.contentHashCode()
        return result
    }
}
