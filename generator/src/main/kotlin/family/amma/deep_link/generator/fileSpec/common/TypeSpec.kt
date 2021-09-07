package family.amma.deep_link.generator.fileSpec.common

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import family.amma.deep_link.generator.main.GeneratorParams
import family.amma.deep_link.generator.entity.*
import family.amma.deep_link.generator.ext.addConstructorWithProps
import family.amma.deep_link.generator.main.nullable

/** Object or data class for [deepLink]. */
internal fun deepLinkTypeSpec(
    parent: ClassName?,
    deepLink: DeepLink,
    destination: DestinationModel,
    className: ClassName,
    params: GeneratorParams
): TypeSpec =
    if (deepLink.args.isNotEmpty()) {
        check(destination.args.isNotEmpty()) {
            "You must define the arguments for the \"${destination.id?.accessor ?: destination.name}\" so that the type can be determined."
        }
        firstNotUsedDestArgWithoutDefaultValue(destination.args, deepLink.args)?.let {
            error("The argument '${it.name}' must contain a default value because not used in deep link.")
        }
        dataClassDeepLinkTypeSpecBuilder(parent, className, destination.args, deepLink)
    } else {
        objectDeepLinkTypeSpecBuilder(parent, className, deepLink)
    }.let { builder ->
        if (params.generateAdditionalInfo) {
            if (deepLink.args.isNotEmpty()) {
                builder.addType(
                    TypeSpec.companionObjectBuilder()
                        .addSuperinterface(DeepLinkAdditionalInfo.className)
                        .addProperties(DeepLinkAdditionalInfo.props(deepLink.uri))
                        .build()
                )
            } else {
                builder
                    .addSuperinterface(DeepLinkAdditionalInfo.className)
                    .addProperties(DeepLinkAdditionalInfo.props(deepLink.uri))
            }
        } else {
            builder
        }
    }
        .build()

/** Check all deep link args and return true, if there is no name match with the [destArgument]. */
private fun noDestArgInDeepLink(destArgument: DestArgument, deepLinkArgs: List<DeepLinkArg>): Boolean {
    val destName = destArgument.name
    return deepLinkArgs.none { deepLinkArg ->
        when (deepLinkArg) {
            is DeepLinkArg.PathParam -> deepLinkArg.name == destName
            is DeepLinkArg.QueryParam -> deepLinkArg.names.any { it == destName }
        }
    }
}

/** @return the first unused destination argument in deep links with no default, or `null` if not. */
private fun firstNotUsedDestArgWithoutDefaultValue(destinationArgs: List<DestArgument>, deepLinkArgs: List<DeepLinkArg>): DestArgument? =
    destinationArgs
        .filter { destArgument -> noDestArgInDeepLink(destArgument, deepLinkArgs) }
        .firstOrNull { it.defaultValue == null }

private fun dataClassDeepLinkTypeSpecBuilder(
    parent: ClassName?, className: ClassName, destinationArgs: List<DestArgument>, deepLink: DeepLink
): TypeSpec.Builder =
    TypeSpec
        .classBuilder(className)
        .addModifiers(KModifier.DATA)
        .addConstructorWithProps(toPropList(deepLink.args, destinationArgs))
        .let { parent?.let(it::superclass) ?: it }
        .addProperty(GeneratedDeepLink.props(deepLink.uri.replace("{", "\${")))

/** Mapping deep link arguments to generation props. */
private fun toPropList(deepLinkArgs: List<DeepLinkArg>, destinationArgs: List<DestArgument>): List<GenerateProp> =
    deepLinkArgs
        .map { arg ->
            when (arg) {
                is DeepLinkArg.PathParam -> listOf(toProp(arg.name, destinationArgs, couldBeNull = false))
                is DeepLinkArg.QueryParam -> arg.names.map { toProp(it, destinationArgs, couldBeNull = true) }
            }
        }
        .flatten()

/** @return the [GenerateProp] by the [name] of the deep link argument in the declared destination argument list. */
private fun toProp(name: String, destinationArgs: List<DestArgument>, couldBeNull: Boolean): GenerateProp =
    destinationArgs.find { name == it.name }
        ?.let { arg ->
            GenerateProp(
                name = name,
                type = if (couldBeNull) arg.type.nullable else arg.type.typeName,
                defaultValue = arg.defaultValue?.write()
            )
        }
        ?: error("No contains the destination argument for the deep link arg \"$name\" in list: ${destinationArgs.map(DestArgument::name)}")

private fun objectDeepLinkTypeSpecBuilder(parent: ClassName?, className: ClassName, deepLink: DeepLink): TypeSpec.Builder =
    TypeSpec
        .objectBuilder(className)
        .let { parent?.let(it::superclass) ?: it }
        .addProperty(GeneratedDeepLink.props(deepLink.uri))
