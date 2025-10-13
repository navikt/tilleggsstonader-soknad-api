package no.nav.tilleggsstonader.soknad.infrastruktur.database

import no.nav.familie.prosessering.PropertiesWrapperTilStringConverter
import no.nav.familie.prosessering.StringTilPropertiesWrapperConverter
import org.postgresql.util.PGobject
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.core.env.Environment
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.annotation.RollbackOn
import javax.sql.DataSource

@Configuration
@EnableJdbcRepositories("no.nav.tilleggsstonader.soknad", "no.nav.familie.prosessering")
@EnableTransactionManagement(rollbackOn = RollbackOn.ALL_EXCEPTIONS)
class DatabaseConfiguration : AbstractJdbcConfiguration() {
    @Bean
    fun operations(dataSource: DataSource): NamedParameterJdbcOperations = NamedParameterJdbcTemplate(dataSource)

    @Bean
    fun transactionManager(dataSource: DataSource): PlatformTransactionManager = DataSourceTransactionManager(dataSource)

    @Bean
    fun verifyIgnoreIfProd(
        @Value("\${spring.flyway.placeholders.ignoreIfProd}") ignoreIfProd: String,
        environment: Environment,
    ): FlywayConfigurationCustomizer {
        val isProd = environment.activeProfiles.contains("prod")
        val ignore = ignoreIfProd == "--"
        return FlywayConfigurationCustomizer {
            if (isProd && !ignore) {
                throw RuntimeException("Prod profile men har ikke riktig verdi for placeholder ignoreIfProd=$ignoreIfProd")
            }
            if (!isProd && ignore) {
                throw RuntimeException("Profile=${environment.activeProfiles} men har ignoreIfProd=--")
            }
        }
    }

    override fun userConverters(): List<*> =
        listOf(
            PGobjectTilJsonWrapperConverter(),
            JsonWrapperTilPGobjectConverter(),
            StringTilPropertiesWrapperConverter(),
            PropertiesWrapperTilStringConverter(),
            ByteArrayWrapperReadingConverter,
            ByteArrayWrapperWritingConverter,
        )

    @ReadingConverter
    class PGobjectTilJsonWrapperConverter : Converter<PGobject, JsonWrapper?> {
        override fun convert(pGobject: PGobject): JsonWrapper? = pGobject.value?.let { JsonWrapper(it) }
    }

    @WritingConverter
    class JsonWrapperTilPGobjectConverter : Converter<JsonWrapper, PGobject> {
        override fun convert(jsonWrapper: JsonWrapper): PGobject =
            PGobject().apply {
                type = "json"
                value = jsonWrapper.json
            }
    }

    @WritingConverter
    object ByteArrayWrapperWritingConverter : Converter<ByteArrayWrapper, ByteArray> {
        override fun convert(source: ByteArrayWrapper): ByteArray = source.data
    }

    @ReadingConverter
    object ByteArrayWrapperReadingConverter : Converter<ByteArray, ByteArrayWrapper> {
        override fun convert(source: ByteArray): ByteArrayWrapper = ByteArrayWrapper(source)
    }
}
