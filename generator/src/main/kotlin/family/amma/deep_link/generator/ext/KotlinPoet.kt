package family.amma.deep_link.generator.ext

import com.squareup.kotlinpoet.*
import family.amma.deep_link.generator.entity.GenerateProp
import family.amma.deep_link.generator.fileSpec.common.GeneratedDeepLink
import family.amma.deep_link.generator.main.NavType

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
internal inline fun <reified T> toListCodeBlock(type: NavType, list: List<T>): CodeBlock =
    CodeBlock
        .builder()
        .addStatement("listOf(${list.joinToString(prefix = "", postfix = "") { type.format }})", *list.toTypedArray())
        .build()

internal fun constValProp(name: String, type: NavType, value: String) =
    PropertySpec
        .builder(name, type.typeName, KModifier.CONST)
        .initializer(type.format, value)
        .build()

internal fun overrideValProp(name: String, type: NavType, value: String?) =
    PropertySpec
        .builder(name, type.typeName.let { if (value == null) it.copy(nullable = true) else it }, KModifier.OVERRIDE)
        .initializer(type.format, value)
        .build()

internal fun listProp(name: String, type: ParameterizedTypeName, value: CodeBlock, vararg modifiers: KModifier) =
    PropertySpec
        .builder(name, type, *modifiers)
        .initializer(value)
        .build()

internal fun deepLinkSealedClass(
    className: ClassName,
    parentClass: ClassName?,
    additional: TypeSpec.Builder.() -> Unit = {}
) = TypeSpec
    .classBuilder(className)
    .addModifiers(KModifier.SEALED)
    .deepLinkSuperType(parentClass)
    .apply(additional)
    .build()

private fun TypeSpec.Builder.deepLinkSuperType(parentClass: ClassName?) =
    if (parentClass != null) {
        superclass(parentClass)
    } else {
        addSuperinterface(GeneratedDeepLink.className)
    }
