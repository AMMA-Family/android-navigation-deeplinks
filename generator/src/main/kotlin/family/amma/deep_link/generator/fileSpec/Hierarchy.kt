package family.amma.deep_link.generator.fileSpec

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import family.amma.deep_link.generator.entity.ApplicationId
import family.amma.deep_link.generator.entity.DeepLink
import family.amma.deep_link.generator.entity.DestinationModel
import family.amma.deep_link.generator.ext.constValProp
import family.amma.deep_link.generator.ext.deepLinkSealedClass
import family.amma.deep_link.generator.ext.endsWithIfNotYet
import family.amma.deep_link.generator.ext.toCamelCase
import family.amma.deep_link.generator.fileSpec.common.camelCaseName
import family.amma.deep_link.generator.fileSpec.common.deepLinkTypeSpec
import family.amma.deep_link.generator.fileSpec.common.indent
import family.amma.deep_link.generator.main.GeneratorParams
import family.amma.deep_link.generator.parser.STRING_FORMAT

/** File for the hierarchical structure of deep links. */
internal fun deepLinksFileSpecHierarchy(
    applicationId: ApplicationId,
    destinations: List<DestinationModel>,
    params: GeneratorParams
): FileSpec {
    val fileName = applicationId.fileName()
    val packageName = applicationId.packageName(fileName)
    return FileSpec.builder(packageName, fileName)
        .indent(indent)
        .also { fileSpec ->
            val rootClassName = ClassName(packageName, fileName)
            fileSpec.addType(
                deepLinkSealedClass(className = rootClassName, parentClass = null) {
                    val deepLinkWithParentDest = destinations
                        .map { destination -> destination.deepLinks.map { it to destination } }
                        .flatten()
                        .toMap()
                    deepLinksTypeSpecListHierarchy(deepLinkWithParentDest, params, parent = rootClassName).forEach(::addType)
                }
            )
        }
        .build()
}

/** In `some://foo/bar/zoo`, the last level is `zoo`.*/
internal fun List<DeepLink>.filterLastLevelDeepLinks(currentSegment: Int): List<DeepLink> =
    filter { it.parsedUri.lastIndex == currentSegment }

/** @return list of file specs for generated deep links. */
private fun deepLinksTypeSpecListHierarchy(
    deepLinkWithParentDest: Map<DeepLink, DestinationModel>,
    params: GeneratorParams,
    parent: ClassName,
    currentSegment: Int = 0
): List<TypeSpec> {

    fun generateHierarchyLevel(
        deepLinkWithParentDest: Map<DeepLink, DestinationModel>,
        className: ClassName,
        originalName: String
    ): TypeSpec = deepLinkSealedClass(className, parent) {
        if (params.generateAdditionalInfo) {
            addType(
                TypeSpec
                    .companionObjectBuilder()
                    .addProperty(
                        constValProp(name = "name", typeToFormat = String::class to STRING_FORMAT, value = originalName)
                    )
                    .build()
            )
        }
        addTypes(deepLinksTypeSpecListHierarchy(deepLinkWithParentDest, params, parent = className, currentSegment + 1))
    }

    return deepLinkWithParentDest
        .keys
        /** Grouping deep links by identical segments on [currentSegment]. */
        .groupBy { deepLink -> deepLink.parsedUri.getOrNull(currentSegment) }
        .map { (key, newDeepLinks) ->
            if (key != null) {
                val nameStr = key.replace('.', '_').toCamelCase()
                val className = ClassName("", nameStr)
                // If there is only one deep link left at the last level, then need to merge it with the last segment.
                newDeepLinks.filterLastLevelDeepLinks(currentSegment).singleOrNull()?.let { deepLink ->
                    val lastLevelDeepLinkName = ClassName("", nameStr.endsWithIfNotYet("DeepLink"))
                    listOfNotNull(
                        deepLinkTypeSpec(
                            parent, deepLink, destination = deepLinkWithParentDest.getValue(deepLink),
                            lastLevelDeepLinkName, params
                        ),
                        deepLinkWithParentDest.minus(deepLink).filterKeys { it in newDeepLinks }.takeIf { it.isNotEmpty() }?.let {
                            generateHierarchyLevel(it, className, originalName = key)
                        }
                    )
                } ?: listOf(
                    generateHierarchyLevel(deepLinkWithParentDest.filterKeys { it in newDeepLinks }, className, originalName = key)
                )
            } else {
                newDeepLinks.map { deepLink ->
                    val nameStr = deepLink.camelCaseName()
                    val name = ClassName("", nameStr.endsWithIfNotYet("DeepLink"))
                    deepLinkTypeSpec(parent, deepLink, destination = deepLinkWithParentDest.getValue(deepLink), name, params)
                }
            }
        }
        .flatten()
}
