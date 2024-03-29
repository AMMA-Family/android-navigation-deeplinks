package family.amma.deep_link.generator.main

import com.squareup.kotlinpoet.FileSpec
import family.amma.deep_link.generator.entity.*
import family.amma.deep_link.generator.entity.DestinationModel
import family.amma.deep_link.generator.entity.ParsedDestination
import family.amma.deep_link.generator.entity.toDestinationModel
import family.amma.deep_link.generator.ext.replace
import family.amma.deep_link.generator.fileSpec.common.DeepLinkAdditionalInfo
import family.amma.deep_link.generator.fileSpec.common.GeneratedDeepLink
import family.amma.deep_link.generator.fileSpec.deepLinksFileSpecByDestinations
import family.amma.deep_link.generator.fileSpec.deepLinksFileSpecHierarchy
import family.amma.deep_link.generator.parser.NavParser
import family.amma.deep_link.generator.parser.XmlPositionParser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader

/**
 * Not pure function that parses xml files, generates code, and writes it to .kt files.
 */
suspend fun generateDeepLinks(
    rFilePackage: String,
    applicationId: ApplicationId,
    navigationXmlFiles: Collection<File>,
    outputDir: File,
    params: GeneratorParams,
    dispatcher: CoroutineDispatcher,
    log: (String) -> Unit = ::println,
) = withContext(dispatcher) {
    if (params.isLoggingEnabled) {
        log("--------------------------------------- Deep links files ---------------------------------------")
        navigationXmlFiles.forEach { log(it.name) }
    }
    if (params.isAsyncParsing) {
        navigationXmlFiles
            .map { navFile -> async(dispatcher) { parseNavigationFile(rFilePackage, applicationId, navFile, dispatcher) } }
            .awaitAll()
            .filterNotNull()
    } else {
        navigationXmlFiles
            .mapNotNull { parseNavigationFile(rFilePackage, applicationId, it, dispatcher) }
    }
        .toFileSpecList(applicationId, params, log)
        .plus(GeneratedDeepLink.fileSpec())
        .let { if (params.generateAdditionalInfo) it.plus(DeepLinkAdditionalInfo.fileSpec()) else it }
        .forEach { fileSpec -> fileSpec.writeTo(outputDir) }
}

/** @return parsed [ParsedDestination] with nested destinations. */
@Suppress("BlockingMethodInNonBlockingContext")
private suspend fun parseNavigationFile(
    rFilePackage: String,
    applicationId: ApplicationId,
    navigationXml: File,
    dispatcher: CoroutineDispatcher
): ParsedDestination? = withContext(dispatcher) {
    FileReader(navigationXml).use { reader ->
        val parser = XmlPositionParser(navigationXml.path, reader)
        parser.traverseStartTags(dispatcher)
        val navParser = NavParser(parser, rFilePackage, applicationId)
        navParser.parseDestination(dispatcher)
    }
}

/** @return file specifications for subsequent generation for [ParsedDestination]. */
private fun List<ParsedDestination>.toFileSpecList(
    applicationId: ApplicationId,
    params: GeneratorParams,
    log: (String) -> Unit
): List<FileSpec> =
    map(::toDestinationList)
        .flatten()
        .filter { it.deepLinks.isNotEmpty() }
        .also {
            if (params.isLoggingEnabled) {
                log("--------------------------------------- Deep links destinations ---------------------------------------")
                for (destination in this) log(destination.toString())
            }
        }
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
