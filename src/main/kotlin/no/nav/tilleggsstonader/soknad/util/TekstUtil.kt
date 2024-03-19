package no.nav.tilleggsstonader.soknad.util

object TekstUtil {

    /**
     * Setter sammen strings som ikke er null eller empty med en gitt separator
     */
    fun joinNotNullOrEmpty(vararg args: String?, separator: String = " "): String? {
        val filterNotNull = args.filterNotNull().filterNot(String::isEmpty)
        return if (filterNotNull.isEmpty()) {
            null
        } else {
            filterNotNull.joinToString(separator)
        }
    }
}
