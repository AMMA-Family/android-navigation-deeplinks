package family.amma.deep_link.generator.ext

import java.util.Locale

/** Convert name from snack case (foo_bar_zoo) to camel case (FooBarZoo). */
internal fun String.toCamelCase(locale: Locale = Locale.getDefault()): String {
    val split: List<String> = this.split("_")
    return when (split.size) {
        0 -> ""
        1 -> split[0].capitalize(locale)
        else -> split.joinToCamelCase(locale)
    }
}

