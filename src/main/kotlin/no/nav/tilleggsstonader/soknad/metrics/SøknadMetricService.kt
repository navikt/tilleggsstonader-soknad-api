package no.nav.tilleggsstonader.soknad.metrics

import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.MultiGauge
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Tags
import no.nav.tilleggsstonader.soknad.soknad.domene.SøknadRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class SøknadMetricService(
    private val søknadRepository: SøknadRepository,
) {
    private val antallRoutingsGauge = MultiGauge.builder("soknader_antall").register(Metrics.globalRegistry)

    @Scheduled(initialDelay = MetricUtil.FREKVENS_30_SEC, fixedDelay = MetricUtil.FREKVENS_30_MIN)
    fun antallRoutings() {
        val rows =
            søknadRepository.finnAntallPerType().map {
                MultiGauge.Row.of(Tags.of(Tag.of("ytelse", it.type.name)), it.count)
            }
        antallRoutingsGauge.register(rows, true)
    }
}
