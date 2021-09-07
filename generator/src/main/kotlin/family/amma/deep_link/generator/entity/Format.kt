package family.amma.deep_link.generator.entity

/** [Formats](https://square.github.io/kotlinpoet/#code-block-format-strings). */
internal sealed class Format(val value: String) {
    override fun toString(): String = value
}

/** [%S for Strings](https://square.github.io/kotlinpoet/#s-for-strings). */
internal object StringFormat : Format(value = "%S")

/** [%P for String Templates](https://square.github.io/kotlinpoet/#p-for-string-templates). */
internal object StringTemplatesFormat : Format(value = "%P")

/** [%L for Literals](https://square.github.io/kotlinpoet/#l-for-literals). */
internal object LiteralsFormat : Format(value = "%L")
