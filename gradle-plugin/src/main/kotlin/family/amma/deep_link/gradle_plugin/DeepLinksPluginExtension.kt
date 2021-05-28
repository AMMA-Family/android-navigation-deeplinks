package family.amma.deep_link.gradle_plugin

import family.amma.deep_link.generator.main.GeneratorParams

open class DeepLinksPluginExtension {
    /** Generation of a separate file with deep links for each destination. */
    var generateByDestinations: Boolean = true

    /** Generating a hierarchy of deep links based on their url. */
    var generateUriHierarchy: Boolean = false

    /** Generation of additional information for all types of generation: names, protocol, host and path segments. */
    var generateAdditionalInfo: Boolean = false

    /** If true - asynchronous parsing of hml files, if false - synchronous. */
    var isAsyncParsing: Boolean = true

    internal companion object {
        const val NAME = "deepLinksPluginExtension"
    }
}

internal fun DeepLinksPluginExtension.toGeneratorParams() =
    GeneratorParams(
        generateByDestinations = generateByDestinations,
        generateUriHierarchy = generateUriHierarchy,
        generateAdditionalInfo = generateAdditionalInfo,
        isAsyncParsing = isAsyncParsing
    )
