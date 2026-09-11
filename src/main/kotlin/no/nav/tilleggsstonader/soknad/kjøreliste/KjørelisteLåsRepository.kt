package no.nav.tilleggsstonader.soknad.kjøreliste

import org.springframework.beans.factory.annotation.Value
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.RowCallbackHandler
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import java.sql.SQLException

@Repository
class KjørelisteLåsRepository(
    private val jdbcOperations: NamedParameterJdbcOperations,
    @Value("\${kjoreliste.laas-timeout-sekunder}") private val låseTimeoutSekunder: Int,
) {
    init {
        require(låseTimeoutSekunder > 0) { "Ventetid for kjøreliste-lås må være positiv" }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    fun låsBruker(ident: String) {
        val tidligereTimeout =
            requireNotNull(
                jdbcOperations.queryForObject("SELECT current_setting('lock_timeout')", emptyMap<String, Any>(), String::class.java),
            )
        settLåseTimeout("${låseTimeoutSekunder}s")

        try {
            jdbcOperations.query(
                "SELECT pg_advisory_xact_lock(:key)",
                mapOf("key" to låsenøkkelForBruker(ident)),
                RowCallbackHandler {},
            )
        } catch (e: DataAccessException) {
            if (generateSequence<Throwable>(e) { it.cause }.filterIsInstance<SQLException>().any { it.sqlState == "55P03" }) {
                throw KjørelisteInnsendingPågårException(e)
            }
            throw e
        }

        // Ved feil er transaksjonen abortert. Rollback tilbakestiller da SET LOCAL automatisk.
        settLåseTimeout(tidligereTimeout)
    }

    private fun settLåseTimeout(timeout: String) {
        jdbcOperations.queryForObject(
            "SELECT set_config('lock_timeout', :timeout, true)",
            mapOf("timeout" to timeout),
            String::class.java,
        )
    }

    companion object {
        internal fun låsenøkkelForBruker(ident: String): Long {
            val hash = MessageDigest.getInstance("SHA-256").digest("kjoreliste-innsending:v1:$ident".toByteArray(Charsets.UTF_8))
            return ByteBuffer.wrap(hash).order(ByteOrder.BIG_ENDIAN).long
        }
    }
}
