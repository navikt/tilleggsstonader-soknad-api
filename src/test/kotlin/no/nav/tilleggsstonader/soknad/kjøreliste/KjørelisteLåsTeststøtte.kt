package no.nav.tilleggsstonader.soknad.kjøreliste

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import java.time.Duration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

internal fun NamedParameterJdbcOperations.antallVedlegg(): Int =
    requireNotNull(queryForObject("SELECT count(*) FROM vedlegg", emptyMap<String, Any>(), Int::class.java))

internal fun NamedParameterJdbcOperations.antallKjørelisteLåser(
    ident: String,
    innvilget: Boolean,
): Int {
    val nøkkel = KjørelisteLåsRepository.låsenøkkelForBruker(ident)
    return requireNotNull(
        queryForObject(
            """
            SELECT count(*) FROM pg_locks
            WHERE locktype = 'advisory'
              AND database = (SELECT oid FROM pg_database WHERE datname = current_database())
              AND classid = :high AND objid = :low AND objsubid = 1
              AND granted = :granted
            """.trimIndent(),
            mapOf("high" to (nøkkel ushr 32), "low" to (nøkkel and 0xffffffffL), "granted" to innvilget),
            Int::class.java,
        ),
    )
}

internal fun NamedParameterJdbcOperations.ventPåVentendeKjørelisteLås(ident: String) {
    await()
        .atMost(Duration.ofSeconds(3))
        .pollInterval(Duration.ofMillis(20))
        .untilAsserted {
            assertThat(antallKjørelisteLåser(ident, innvilget = false)).isEqualTo(1)
        }
}

internal fun <T> medKjørelisteExecutor(block: (ExecutorService) -> T): T {
    val executor = Executors.newFixedThreadPool(2)
    try {
        return block(executor)
    } finally {
        executor.shutdownNow()
        assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue()
    }
}
