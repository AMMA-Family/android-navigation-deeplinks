package family.amma.deep_link.generator.entity

import family.amma.deep_link.generator.main.NavType
import family.amma.deep_link.generator.parser.NullValue
import family.amma.deep_link.generator.parser.WriteableValue

/**
 * Argument of destination in xml.
 * Example:
 * ```xml
 * <argument
 *   android:name="data"
 *   app:argType="string"
 *   android:defaultValue="fooBar"
 *   app:nullable="true" />
 * ```
 */
internal data class DestArgument(
    val name: String,
    val type: NavType,
    val defaultValue: WriteableValue? = null,
    val isNullable: Boolean = false
) {
    init {
        if (isNullable && !type.allowsNullable) {
            throw IllegalArgumentException("Argument is nullable but type ${type.typeName} cannot be nullable.")
        }
        if (!isNullable && defaultValue == NullValue) {
            throw IllegalArgumentException("Argument has null value but is not nullable.")
        }
    }
}
