package family.amma.deep_link.generator.main

import java.io.Serializable

/**
 * @param generateByDestinations Generation of a separate file with deep links for each destination.
 * @param generateUriHierarchy Generating a hierarchy of deep links based on their url. Important: this flag disables incremental compilation.
 * @param generateAdditionalInfo Generation of additional information for all types of generation: names, protocol, host and path segments.
 * @param isAsyncParsing If true - asynchronous parsing of hml files, false - synchronous.
 * @param isLoggingEnabled Is logging enabled for debugging.
 */
data class GeneratorParams(
    val generateByDestinations: Boolean,
    val generateUriHierarchy: Boolean,
    val generateAdditionalInfo: Boolean,
    val isAsyncParsing: Boolean,
    val isLoggingEnabled: Boolean
) : Serializable
