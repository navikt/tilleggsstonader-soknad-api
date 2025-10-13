package no.nav.tilleggsstonader.soknad.infrastruktur.database

data class ByteArrayWrapper(
    val data: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ByteArrayWrapper

        return data.contentEquals(other.data)
    }

    override fun hashCode(): Int = data.contentHashCode()
}
