package no.nav.tilleggsstonader.soknad.soknad.domene

import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.soknad.infrastruktur.database.JsonWrapper
import no.nav.tilleggsstonader.soknad.infrastruktur.database.SporbarUtils
import no.nav.tilleggsstonader.soknad.infrastruktur.database.repository.InsertUpdateRepository
import no.nav.tilleggsstonader.soknad.infrastruktur.database.repository.RepositoryInterface
import org.springframework.data.annotation.Id
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

    val journalpostId: String? = null,
)
