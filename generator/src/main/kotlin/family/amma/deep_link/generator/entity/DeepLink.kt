package family.amma.deep_link.generator.entity

import family.amma.deep_link.generator.ResReference
import java.net.URI

/**
 * Parsed deep link model.
 * @param id Id reference to deep link.
 * @param uri Link for navigation.
 * @param args Arguments to be substituted into [uri].
 * @param parsedUri list of protocol, host and path segments.
 */
internal data class DeepLink(
    val id: ResReference,
    val uri: Uri,
    val args: List<DeepLinkArg>,
    val parsedUri: List<String>
)

internal typealias Uri = String

internal fun Uri.header() = UriHeader(split("://"))

@JvmInline
internal value class UriHeader(private val header: List<String>) {
    /** @return first part before '://' or `null`. */
    fun protocol(): String? =
        header.takeIf { it.size > 1 }?.first()

    /** @return first part after '://'. */
    fun host(): String? =
        header.drop(1).firstOrNull()?.takeWhile { it != '{' && it != '?' && it != '/' }
}

/** @return list of protocol, host and path segments. */
internal fun Uri.parsed(): List<String> =
    with(trimPartToParameters()) {
        val header = header()
        listOfNotNull(header.protocol(), header.host()) + pathSegments()
    }

/**
 * Example:
 * ```
 * http://www.example.com/users/{id}?isEditMode={isEditMode}&data={data}
 * ```
 *
 * Result:
 * ```
 * http://www.example.com/users/
 * ```
 */
fun Uri.trimPartToParameters() = takeWhile { it != '{' && it != '?' }

/** @return parsed path of uri. */
fun Uri.pathSegments(): List<String> =
    URI(this).path.split('/').filter(String::isNotEmpty)

sealed class DeepLinkArg {
    /**
     * Path parameter placeholders in the form of `{placeholder_name}` match one or more characters.
     * For example, `http://www.example.com/users/{id}` matches `http://www.example.com/users/4`.
     * The Navigation component attempts to parse the placeholder values into appropriate types by matching placeholder names
     * to the defined arguments that are defined for the deep link destination.
     * If no argument with the same name is defined, a default String type is used for the argument value.
     * You can use the .* wildcard to match 0 or more characters.
     */
    data class PathParam(val name: String) : DeepLinkArg()

    /**
     * Query parameter placeholders can be used instead of or in conjunction with path parameters.
     * For example, `http://www.example.com/users/{id}?arg1={arg1}&arg2={arg2}` matches:
     * `http://www.example.com/users/4?arg2=28` or `http://www.example.com/users/4?arg1=7` or `http://www.example.com/users/4?arg1=7&arg2=28.
     */
    data class QueryParam(val names: List<String>) : DeepLinkArg()
}
