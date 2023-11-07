package no.nav.tilleggsstonader.soknad.util

object EnvUtil {
    fun erIProd() = System.getenv("NAIS_CLUSTER_NAME") == "prod-gcp"
}
