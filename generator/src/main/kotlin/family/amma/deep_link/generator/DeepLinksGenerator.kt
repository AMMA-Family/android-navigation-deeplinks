package family.amma.deep_link.generator

import com.squareup.kotlinpoet.FileSpec
import family.amma.deep_link.generator.entity.DestinationModel
import family.amma.deep_link.generator.entity.ParsedDestination
import family.amma.deep_link.generator.entity.plus
import family.amma.deep_link.generator.entity.toDestinationModel
import family.amma.deep_link.generator.ext.io
import family.amma.deep_link.generator.ext.replace
import family.amma.deep_link.generator.fileSpec.common.generatedDeepLinkFileSpec
import family.amma.deep_link.generator.fileSpec.deepLinksFileSpecByDestinations
import family.amma.deep_link.generator.fileSpec.deepLinksFileSpecHierarchy
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.io.File
import java.io.FileReader
import java.io.Serializable

/**
 * @param generateByDestinations Generation of a separate file with deep links for each destination.
 * @param generateUriHierarchy Generating a hierarchy of deep links based on their url.
 * @param generateAdditionalInfo Generation of additional information for all types of generation: names, protocol, host and path segments.
 */
data class GeneratorParams(
    val generateByDestinations: Boolean,
    val generateUriHierarchy: Boolean,
    val generateAdditionalInfo: Boolean
) : Serializable

/**
 * Not pure function that parses xml files, generates code, and writes it to .kt files.
 */
suspend fun generateDeepLinks(
    rFilePackage: String,
    applicationId: String,
    navigationXmlFiles: List<File>,
    outputDir: File,
    params: GeneratorParams
) = io {
    navigationXmlFiles
        .mapNotNull { parseNavigationFile(rFilePackage, applicationId, it) }
        .map { parsedDestination -> async { parsedDestination.toFileSpecList(applicationId, params) } }
        .awaitAll()
        .flatten()
        .plus(generatedDeepLinkFileSpec())
        .forEach { outputDir.write(it) }
}

@Suppress("BlockingMethodInNonBlockingContext")
private suspend fun File.write(fileSpec: FileSpec) = io {
    fileSpec.writeTo(this@write)
}

/** @return parsed [ParsedDestination] with nested destinations. */
@Suppress("BlockingMethodInNonBlockingContext")
private suspend fun parseNavigationFile(
    rFilePackage: String,
    applicationId: String,
    navigationXml: File
): ParsedDestination? = io {
    FileReader(navigationXml).use { reader ->
        val parser = XmlPositionParser(navigationXml.path, reader)
        parser.traverseStartTags()
        val navParser = NavParser(parser, rFilePackage, applicationId)
        navParser.parseDestination()
    }
}

/** @return file specifications for subsequent generation for [ParsedDestination]. */
private fun ParsedDestination.toFileSpecList(applicationId: String, params: GeneratorParams): List<FileSpec> =
    toDestinationList(this)
        .filter { it.deepLinks.isNotEmpty() }
        .let(::merge)
        .let { destinations: List<DestinationModel> ->
            listOf(
                if (params.generateByDestinations) destinations.map { deepLinksFileSpecByDestinations(it, params) } else emptyList(),
                if (params.generateUriHierarchy) {
                    listOf(deepLinksFileSpecHierarchy(applicationId, destinations, params))
                } else {
                    emptyList()
                }
            ).flatten()
        }

/** @return a list of destinations without nesting. */
private fun toDestinationList(destination: ParsedDestination): List<DestinationModel> =
    listOf(destination.toDestinationModel()) + destination.nested.map(::toDestinationList).flatten()

/** @return a list of directions with merged deep links if there were duplicate directions. */
private fun merge(destinations: List<DestinationModel>): List<DestinationModel> =
    destinations.fold(emptyList()) { acc: List<DestinationModel>, destination ->
        val existing = acc.firstOrNull { it.simpleName == destination.simpleName && it != destination }
        if (existing != null) {
            acc.replace(old = existing, new = existing + destination)
        } else {
            acc + destination
        }
    }
