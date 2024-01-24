package no.nav.tilleggsstonader.soknad.varsel

import no.nav.tilleggsstonader.libs.log.SecureLogger.secureLogger
import no.nav.tms.varsel.action.EksternKanal
import no.nav.tms.varsel.action.EksternVarslingBestilling
import no.nav.tms.varsel.action.Sensitivitet
import no.nav.tms.varsel.action.Tekst
import no.nav.tms.varsel.action.Varseltype
import no.nav.tms.varsel.builder.VarselActionBuilder
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class DittNavKafkaProducer(val kafkaTemplate: KafkaTemplate<String, String>) {

    @Value("\${KAFKA_TOPIC_DITTNAV}")
    private lateinit var topic: String

    fun sendToKafka(
        fnr: String,
        melding: String,
        eventId: String,
    ): String {
        val kafkaBeskjedJson = VarselActionBuilder.opprett {
            type = Varseltype.Beskjed
            varselId = eventId
            sensitivitet = Sensitivitet.Substantial
            ident = fnr
            tekster += Tekst(
                spraakkode = "nb",
                tekst = melding,
                default = true,
            )
            eksternVarsling = EksternVarslingBestilling(prefererteKanaler = listOf(EksternKanal.EPOST, EksternKanal.SMS))
        }

        runCatching {
            val producerRecord = ProducerRecord(topic, eventId, kafkaBeskjedJson)
            kafkaTemplate.send(producerRecord).get()
        }.onFailure {
            val errorMessage = "Could not send DittNav to Kafka. Check secure logs for more information."
            secureLogger.error("Could not send DittNav to Kafka melding=$kafkaBeskjedJson", it)
            throw RuntimeException(errorMessage)
        }
        return kafkaBeskjedJson
    }
}
