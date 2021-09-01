package family.amma.deep_link.gradle_plugin

import family.amma.deep_link.generator.main.GeneratorParams

open class DeepLinksPluginExtension {
    /** Generation of a separate file with deep links for each destination. */
    var generateByDestinations: Boolean = true

    /** Generating a hierarchy of deep links based on their url. Important: this flag disables incremental compilation. */
    var generateUriHierarchy: Boolean = false

    /** Generation of additional information for all types of generation: names, protocol, host and path segments. */
    var generateAdditionalInfo: Boolean = false

    /** If true - asynchronous parsing of hml files, if false - synchronous. */
    var isAsyncParsing: Boolean = true

    /** Is logging enabled for debugging. */
    var isLoggingEnabled: Boolean = false

    internal companion object {
        const val NAME = "deepLinksPluginExtension"
    }
}

internal fun DeepLinksPluginExtension.toGeneratorParams() =
    GeneratorParams(
        generateByDestinations = generateByDestinations,
        generateUriHierarchy = generateUriHierarchy,
        generateAdditionalInfo = generateAdditionalInfo,
        isAsyncParsing = isAsyncParsing,
        isLoggingEnabled = isLoggingEnabled
    )
