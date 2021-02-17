package family.amma.deep_link.generator.fileSpec

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.KModifier
import family.amma.deep_link.generator.GeneratorParams
import family.amma.deep_link.generator.entity.DestinationModel
import family.amma.deep_link.generator.fileSpec.common.deepLinkTypeSpec
import family.amma.deep_link.generator.fileSpec.common.camelCaseName
import family.amma.deep_link.generator.fileSpec.common.indent

internal val generatedDeepLink = ClassName("deep_link", "GeneratedDeepLink")

internal const val DEEP_LINK_URI_PROP_NAME = "uri"

/** File for [destination]. */
internal fun deepLinksFileSpecByDestinations(destination: DestinationModel, params: GeneratorParams): FileSpec {
    val destName = destination.name
    val className = ClassName(destName.packageName, "${destName.simpleName}DeepLink")
    val typeSpec = destinationDeepLinksTypeSpec(className, destination, params)
    return FileSpec.builder(className.packageName, className.simpleName)
        .indent(indent)
        .addType(typeSpec)
        .build()
}

/** Sealed class with children for [destination]. */
private fun destinationDeepLinksTypeSpec(className: ClassName, destination: DestinationModel, params: GeneratorParams): TypeSpec =
    TypeSpec
        .classBuilder(className)
        .addModifiers(KModifier.SEALED)
        .addTypes(destination.deepLinks.map { deepLink ->
            val name = ClassName("", deepLink.camelCaseName())
            deepLinkTypeSpec(parent = className, deepLink, destination, name, params)
        })
        .addSuperinterface(generatedDeepLink)
        .build()
