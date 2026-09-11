package no.nav.tilleggsstonader.soknad.infrastruktur.database

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.stereotype.Service

/**
 * Wrapper rundt Postgres sine advisory locks - brukes til å serialisere samtidige
 * operasjoner på tvers av pod-instanser for en gitt nøkkel (f.eks. en brukers personIdent),
 * uten å måtte innføre en egen låsetabell.
 *
 * `pg_advisory_xact_lock` er transaksjons-scopet: låsen tas og slippes automatisk ved
 * commit/rollback av den omkringliggende transaksjonen, så det er ingen fare for hengende
 * lås ved krasj. Kallende kode må derfor selv sørge for at kallet skjer innenfor en
 * `@Transactional`-metode.
 */
@Service
class AdvisoryLockService(
    private val jdbcOperations: NamedParameterJdbcOperations,
) {
    /**
     * Tar en transaksjons-scopet advisory lock for [nøkkel]. Blokkerer til låsen er ledig,
     * og slippes automatisk når den omkringliggende transaksjonen committer eller rulles tilbake.
     */
    fun taLåsForTransaksjon(nøkkel: String) {
        jdbcOperations.execute(
            "SELECT pg_advisory_xact_lock(hashtext(:nokkel))",
            MapSqlParameterSource("nokkel", nøkkel),
        ) { preparedStatement -> preparedStatement.execute() }
    }
}
