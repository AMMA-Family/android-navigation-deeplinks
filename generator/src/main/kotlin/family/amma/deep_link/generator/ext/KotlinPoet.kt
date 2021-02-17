package family.amma.deep_link.generator.ext

import com.squareup.kotlinpoet.CodeBlock
import family.amma.deep_link.generator.entity.GenerateProp
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import family.amma.deep_link.generator.STRING_FORMAT

/**
 * Example:
 * ```
 * TypeSpec
 *   .classBuilder("Foo")
 *   .addConstructorWithProp<String>("bar", KModifier.PRIVATE)
 *   .build()
 * ```
 *
 * Result:
 * `class Foo(private val bar: String)`
 */
internal inline fun <reified Type> TypeSpec.Builder.addConstructorWithProp(name: String, vararg modifiers: KModifier): TypeSpec.Builder {
    primaryConstructor(
        FunSpec.constructorBuilder()
            .addParameter(name, Type::class)
            .build()
    )
    addProperty(
        PropertySpec.builder(name, Type::class)
            .initializer(name)
            .addModifiers(modifiers.asIterable())
            .build()
    )
    return this
}

/**
 * Example:
 * ```
 * TypeSpec
 *   .classBuilder("Clazz")
 *   .addConstructorWithProps(
 *      listOf(
 *          GenerateProp(name = "foo", typeName = Int::class.asTypeName(), defaultValue = CodeBlock.of("10")),
 *          GenerateProp(name = "bar", typeName = String::class.asTypeName().copy(isNullable = true), defaultValue = CodeBlock.of("null"))
 *      )
 *   )
 *   .build()
 * ```
 *
 * Result:
 * ```
 * class Clazz(
 *   val foo: Int = 10,
 *   val bar: String? = null
 * )
 * ```
 */
internal fun TypeSpec.Builder.addConstructorWithProps(props: List<GenerateProp>): TypeSpec.Builder {
    val constructorBuilder = FunSpec.constructorBuilder()
    for (prop in props) {
        constructorBuilder.addParameter(
            ParameterSpec.builder(prop.name, prop.typeName)
                .also { prop.defaultValue?.let(it::defaultValue) }
                .build()
        )
        addProperty(
            PropertySpec.builder(prop.name, prop.typeName)
                .initializer(prop.name)
                .build()
        )
    }
    primaryConstructor(constructorBuilder.build())
    return this
}

/**
 * Example:
 * ```
 * val list = listOf("lol", "kek", "azaza")
 * toListCodeBlock("%S", list)
 * ```
 *
 * Result:
 * ```
 * listOf("lol", "kek", "azaza")
 * ```
 */
inline fun <reified T> toListCodeBlock(format: String, list: List<T>): CodeBlock =
    CodeBlock
        .builder()
        .addStatement("listOf(${list.joinToString(prefix = "", postfix = "") { format }})", *list.toTypedArray())
        .build()

internal fun constValProp(name: String, value: String) =
    PropertySpec
        .builder(name, String::class, KModifier.CONST)
        .initializer(STRING_FORMAT, value)
        .build()

internal fun listProp(name: String, value: CodeBlock) =
    PropertySpec
        .builder(name, List::class.parameterizedBy(String::class))
        .initializer(value)
        .build()
