package family.amma.deep_link.generator.ext

import java.util.Locale

/** Convert name from snack case (foo_bar_zoo) to camel case (FooBarZoo). */
fun List<String>.joinToCamelCase(locale: Locale): String = when (size) {
    0 -> throw IllegalArgumentException("invalid section size, cannot be zero")
    1 -> this[0].toCamelCase(locale)
    else -> this.joinToString("") { it.toCamelCase(locale) }
}

/**
 * @return filter unique elements.
 * Same as [List.distinct], but preserves order.
 */
fun <T> Iterable<T>.filterUnique(): List<T> {
    val accumulator = mutableSetOf<T>()
    return filter(accumulator::add)
}

/** Replaces all occurrences [old] on [new]. */
fun <E> Iterable<E>.replace(old: E, new: E): List<E> =
    toMutableList().also { it[it.indexOf(old)] = new }
