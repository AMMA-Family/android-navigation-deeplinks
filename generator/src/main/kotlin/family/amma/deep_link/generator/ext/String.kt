package family.amma.deep_link.generator.ext

import java.util.Locale

private val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()
private val snakeRegex = "_[a-zA-Z]".toRegex()

/** Convert name from camel case (FooBarZoo) to snack case (foo_bar_zoo). */
fun String.toSnakeCase(locale: Locale = Locale.getDefault()): String =
    camelRegex.replace(this) { "_${it.value}" }.lowercase(locale)

/** Convert name from snack case (foo_bar_zoo) to camel case (FooBarZoo). */
fun String.toCamelCase(locale: Locale = Locale.getDefault()): String =
    snakeToUpperCamelCase(locale)

/** Convert name from snack case (foo_bar_zoo) to camel case (FooBarZoo). */
private fun String.snakeToUpperCamelCase(locale: Locale = Locale.getDefault()): String =
    snakeToLowerCamelCase(locale).replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }

/** Convert name from snack case (foo_bar_zoo) to camel case (fooBarZoo). */
private fun String.snakeToLowerCamelCase(locale: Locale): String =
    snakeRegex.replace(this) {
        it.value.replace("_", "").uppercase(locale)
    }