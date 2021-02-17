package family.amma.deep_link.gradle_plugin

import family.amma.deep_link.generator.GeneratorParams

open class DeepLinksPluginExtension {
    var generateByDestinations: Boolean = true
    var generateUriHierarchy: Boolean = false
    var generateAdditionalInfo: Boolean = false
    var isIncrementalEnabled: Boolean = true

    internal companion object {
        const val NAME = "deepLinksPluginExtension"
    }
}

internal fun DeepLinksPluginExtension.toGeneratorParams() =
    GeneratorParams(
        generateByDestinations = generateByDestinations,
        generateUriHierarchy = generateUriHierarchy,
        generateAdditionalInfo = generateAdditionalInfo
    )
