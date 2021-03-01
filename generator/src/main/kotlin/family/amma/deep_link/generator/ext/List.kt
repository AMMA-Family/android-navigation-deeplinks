package family.amma.deep_link.generator.ext

/**
 * @return filter unique elements.
 * Same as [Iterable.distinct], but preserves order.
 */
fun <T> Iterable<T>.filterUnique(): List<T> {
    val accumulator = mutableSetOf<T>()
    return filter(accumulator::add)
}

/** Replaces all occurrences [old] on [new]. */
fun <E> Iterable<E>.replace(old: E, new: E): List<E> =
    toMutableList().also { it[it.indexOf(old)] = new }
