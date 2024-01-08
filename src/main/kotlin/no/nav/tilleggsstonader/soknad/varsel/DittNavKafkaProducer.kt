package no.nav.tilleggsstonader.soknad.varsel

import no.nav.brukernotifikasjon.schemas.builders.BeskjedInputBuilder
import no.nav.brukernotifikasjon.schemas.builders.NokkelInputBuilder
import no.nav.brukernotifikasjon.schemas.builders.domain.PreferertKanal
import no.nav.brukernotifikasjon.schemas.input.BeskjedInput
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import no.nav.tilleggsstonader.libs.log.SecureLogger.secureLogger
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC

@Service
class DittNavKafkaProducer(private val kafkaTemplate: KafkaTemplate<NokkelInput, BeskjedInput>) {

    @Value("\${KAFKA_TOPIC_DITTNAV}")
    private lateinit var topic: String

    fun sendToKafka(
        fnr: String,
        melding: String,
        eventId: String,
        link: URL? = null,
        kanal: PreferertKanal? = null,
    ) {
        val nokkel = lagNøkkel(fnr, eventId)
        val beskjed = lagBeskjed(melding, kanal)

        secureLogger.debug("Sending to Kafka topic: {}: {}", topic, beskjed)
        runCatching {
            val producerRecord = ProducerRecord(topic, nokkel, beskjed)
            kafkaTemplate.send(producerRecord).get()
        }.onFailure {
            val errorMessage = "Could not send DittNav to Kafka. Check secure logs for more information."
            secureLogger.error("Could not send DittNav to Kafka melding={}", beskjed, it)
            throw RuntimeException(errorMessage)
        }
    }
    private fun lagNøkkel(fnr: String, eventId: String): NokkelInput =
        NokkelInputBuilder()
            .withAppnavn("tilleggsstonader-soknad-api")
            .withNamespace("tilleggsstonader")
            .withFodselsnummer(fnr)
            .withEventId(eventId)
            .build()

    private fun lagBeskjed(melding: String, kanal: PreferertKanal?): BeskjedInput {
        val builder = BeskjedInputBuilder()
            .withSikkerhetsnivaa(4)
            .withSynligFremTil(null)
            .withTekst(melding)
            .withTidspunkt(LocalDateTime.now(UTC))
        builder.withEksternVarsling(true).withPrefererteKanaler(kanal)
        return builder.build()
    }
}
