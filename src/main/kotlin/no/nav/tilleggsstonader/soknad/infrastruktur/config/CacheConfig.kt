package no.nav.tilleggsstonader.soknad.infrastruktur.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCache
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.util.concurrent.TimeUnit

@Configuration
@EnableCaching
class CacheConfig {

    /**
     * Må ha en primary når man har flere cacher
     */
    @Bean("shortCache")
    @Primary
    fun cacheManager(): CacheManager = object : ConcurrentMapCacheManager() {
        override fun createConcurrentMapCache(name: String): Cache {
            val concurrentMap = Caffeine
                .newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats().build<Any, Any>().asMap()
            return ConcurrentMapCache(name, concurrentMap, true)
        }
    }

    @Bean("aktivitetCache")
    fun aktivitetCache(): CacheManager = object : ConcurrentMapCacheManager() {
        override fun createConcurrentMapCache(name: String): Cache {
            val concurrentMap = Caffeine
                .newBuilder()
                .maximumSize(100)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats().build<Any, Any>().asMap()
            return ConcurrentMapCache(name, concurrentMap, true)
        }
    }

    @Bean("kodeverkCache")
    fun kodeverkCache(): CacheManager = object : ConcurrentMapCacheManager() {
        override fun createConcurrentMapCache(name: String): Cache {
            val concurrentMap = Caffeine
                .newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(24, TimeUnit.HOURS)
                .recordStats().build<Any, Any>().asMap()
            return ConcurrentMapCache(name, concurrentMap, true)
        }
    }
}
