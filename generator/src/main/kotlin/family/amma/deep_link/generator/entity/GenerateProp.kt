package family.amma.deep_link.generator.entity

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeName
import family.amma.deep_link.generator.main.NavType

/**
 * Generated property in deep links.
 * Example:
 * ```kotlin
 * data class DeepLink(
 *     val foo: String? = null // <- property
 * )
 * ```
 * @param defaultValue `null` if no default value exists.
 */
internal data class GenerateProp(val name: String, val typeName: TypeName, val defaultValue: CodeBlock?) {
    constructor(name: String, type: NavType, defaultValue: CodeBlock?) : this(name, type.typeName, defaultValue)
}
