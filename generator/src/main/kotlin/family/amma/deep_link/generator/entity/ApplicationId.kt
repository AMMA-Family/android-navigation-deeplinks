package family.amma.deep_link.generator.entity

import family.amma.deep_link.generator.ext.toCamelCase

@JvmInline
value class ApplicationId(private val value: String) {
    fun packageName(fileName: String): String =
        value.dropLast(fileName.length + 1) // +1 because the point needs to be removed.

    fun fileName(): String =
        value.substringAfterLast('.').toCamelCase()

    override fun toString(): String = value
}
