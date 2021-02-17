package family.amma.deep_link.generator

import family.amma.deep_link.generator.entity.NavParserErrors.sameSanitizedNameDeepLinks
import family.amma.deep_link.generator.ext.toCamelCase
import family.amma.deep_link.generator.ext.showError
import com.squareup.kotlinpoet.ClassName
import family.amma.deep_link.generator.entity.*
import family.amma.deep_link.generator.entity.DeepLink
import family.amma.deep_link.generator.entity.DestArgument
import family.amma.deep_link.generator.entity.NavParserErrors
import family.amma.deep_link.generator.entity.ParsedDestination
import family.amma.deep_link.generator.entity.XmlPosition

private const val TAG_NAVIGATION = "navigation"
private const val TAG_DEEP_LINK = "deepLink"
private const val TAG_ARGUMENT = "argument"
private const val TAG_FRAGMENT = "fragment"

private const val ATTRIBUTE_DEFAULT_VALUE = "defaultValue"
private const val ATTRIBUTE_TYPE = "argType"
private const val ATTRIBUTE_TYPE_DEPRECATED = "type"
private const val ATTRIBUTE_NULLABLE = "nullable"

const val VALUE_NULL = "@null"
private const val VALUE_TRUE = "true"
private const val VALUE_FALSE = "false"

private const val ATTRIBUTE_ID = "id"
private const val ATTRIBUTE_URI = "uri"
private const val ATTRIBUTE_NAME = "name"

private const val NAMESPACE_RES_AUTO = "http://schemas.android.com/apk/res-auto"
private const val NAMESPACE_ANDROID = "http://schemas.android.com/apk/res/android"

/** Wrapper over [XmlPositionParser] to parse [ParsedDestination] from xml. */
internal class NavParser(
    private val parser: XmlPositionParser,
    private val rFilePackage: String,
    private val applicationId: String
) {
    /** @return parsed [ParsedDestination] from xml. */
    fun parseDestination(): ParsedDestination? {
        val position = parser.xmlPosition()
        val name = parser.attrValue(NAMESPACE_ANDROID, ATTRIBUTE_NAME)
        val idValue = parser.attrValue(NAMESPACE_ANDROID, ATTRIBUTE_ID)
        val deepLinks = mutableListOf<DeepLink>()
        val args = mutableListOf<DestArgument>()
        val nested = mutableListOf<ParsedDestination>()
        parser.traverseInnerStartTags {
            when (parser.tag()) {
                TAG_DEEP_LINK -> deepLinks.add(parseDeepLink())
                TAG_ARGUMENT -> args.add(parseArgument())
                TAG_NAVIGATION, TAG_FRAGMENT -> parseDestination()?.let(nested::add)
            }
        }

        deepLinks.groupBy { it.id.identifier.toCamelCase() }.forEach { (sanitizedName, deepLinks) ->
            if (deepLinks.size > 1) {
                showError(sameSanitizedNameDeepLinks(sanitizedName, deepLinks), position)
            }
        }

        val id = idValue?.let { parseId(idValue, rFilePackage, position) }
        val className = createName(name, applicationId, id)
        return if (className == null) {
            if (deepLinks.isNotEmpty() || args.isNotEmpty() || nested.isNotEmpty()) {
                showError(NavParserErrors.UNKNOWN_DESTINATION, position)
            } else {
                null
            }
        } else {
            ParsedDestination(id, className, deepLinks, args, nested)
        }
    }

    /** @return parsed [DestArgument] from xml or `null` if the argument is to be ignored. */
    private fun parseArgument(): DestArgument {
        val xmlPosition = parser.xmlPosition()
        val name = parser.attrValueOrError(NAMESPACE_ANDROID, ATTRIBUTE_NAME)
        val defaultValue = parser.attrValue(NAMESPACE_ANDROID, ATTRIBUTE_DEFAULT_VALUE)
        val typeString = parser.attrValue(NAMESPACE_RES_AUTO, ATTRIBUTE_TYPE)
        val nullable = parser.attrValue(NAMESPACE_RES_AUTO, ATTRIBUTE_NULLABLE)?.let { it == VALUE_TRUE } ?: false

        if (parser.attrValue(NAMESPACE_RES_AUTO, ATTRIBUTE_TYPE_DEPRECATED) != null) {
            showError(NavParserErrors.deprecatedTypeAttrUsed(name), xmlPosition)
        }

        if (typeString == null && defaultValue != null) {
            return inferArgument(name, defaultValue, rFilePackage)
        }

        val type = NavType.from(typeString)
        if (nullable && !type.allowsNullable()) {
            showError(NavParserErrors.typeIsNotNullable(typeString), xmlPosition)
        }

        if (defaultValue == null) {
            return DestArgument(name, type, null, nullable)
        }

        val defaultTypedValue = when (type) {
            IntType -> parseIntValue(defaultValue)
            LongType -> parseLongValue(defaultValue)
            FloatType -> parseFloatValue(defaultValue)
            BoolType -> parseBoolean(defaultValue)
            ReferenceType -> parseReference(defaultValue, rFilePackage)?.let(::ReferenceValue)
            StringType -> if (defaultValue == VALUE_NULL) NullValue else StringValue(defaultValue)
        }

        if (defaultTypedValue == null) {
            val errorMessage = when (type) {
                ReferenceType -> NavParserErrors.invalidDefaultValueReference(defaultValue)
                else -> NavParserErrors.invalidDefaultValue(defaultValue, type)
            }
            showError(errorMessage, xmlPosition)
        }

        if (!nullable && defaultTypedValue == NullValue) {
            showError(NavParserErrors.defaultNullButNotNullable(name), xmlPosition)
        }

        return DestArgument(name, type, defaultTypedValue, nullable)
    }

    /** @return parsed [DeepLink] from xml. */
    private fun parseDeepLink(): DeepLink {
        val idValue = parser.attrValue(NAMESPACE_ANDROID, ATTRIBUTE_ID)
        val uriValue = parser.attrValueOrError(NAMESPACE_RES_AUTO, ATTRIBUTE_URI)
        val position = parser.xmlPosition()
        parser.traverseInnerStartTags()

        val id = if (idValue != null) {
            parseId(idValue, rFilePackage, position)
        } else {
            showError("The deep link (\"$uriValue\") must contain id", position)
        }
        return DeepLink(id, uriValue, getArgs(uriValue, position), uriValue.parsed())
    }
}

/** @return camel case name for destination. */
private fun createName(name: String?, applicationId: String, id: ResReference?): ClassName? =
    if (name != null && name.isNotEmpty()) {
        val specifiedPackage = name.substringBeforeLast('.', "")
        val classPackage = if (name.startsWith(".")) {
            "$applicationId$specifiedPackage"
        } else {
            specifiedPackage
        }
        ClassName(classPackage, name.substringAfterLast('.'))
    } else if (id != null) {
        ClassName(id.packageName, id.identifier.toCamelCase())
    } else {
        null
    }

/** @return id in [ResReference] or stub. */
private fun parseId(
    xmlId: String,
    rFilePackage: String,
    xmlPosition: XmlPosition
): ResReference =
    parseReference(xmlId, rFilePackage)?.takeIf(ResReference::isId) ?: showError(NavParserErrors.invalidId(xmlId), xmlPosition)

// @[+][package:]id/resource_name -> package.R.id.resource_name
private val RESOURCE_REGEX = Regex("^@[+]?(.+?:)?(.+?)/(.+)$")

/** @[+][package:]id/resource_name -> package.R.id.resource_name */
internal fun parseReference(xmlValue: String, rFilePackage: String): ResReference? =
    RESOURCE_REGEX.matchEntire(xmlValue)?.let { matchEntire ->
        val groups = matchEntire.groupValues
        val resourceName = groups.last()
        val resType = groups[groups.size - 2]
        val packageName = groups[1].takeIf { it.isNotEmpty() }?.removeSuffix(":") ?: rFilePackage
        ResReference(packageName, resType, resourceName)
    }

/** @return args list from [uri] or empty list. */
private fun getArgs(uri: String, position: XmlPosition): List<DeepLinkArg> {
    val part = uri.dropWhile { it != '{' && it != '?' }
    return part.firstOrNull()?.let { char ->
        when (char) {
            '{' ->
                listOf(DeepLinkArg.PathParam(takeInsideCurlyBraces(part))) + getArgs(takeAfterCurlyBraces(part), position)

            '?' ->
                listOf(DeepLinkArg.QueryParam(getQueryParamsNames(part.drop(1))))

            else -> showError("Unknown char: $char", position)
        }
    } ?: emptyList()
}

/** @return aaa{foo}bbb -> foo */
private fun takeInsideCurlyBraces(string: String): String =
    string.dropWhile { it != '{' }.drop(1).takeWhile { it != '}' }

/** @return aaa{foo}bar -> bar */
private fun takeAfterCurlyBraces(string: String): String =
    string.dropWhile { it != '}' }.drop(1)

/** @return list of query params names. */
private fun getQueryParamsNames(lastPart: String): List<String> =
    if (lastPart.isNotEmpty()) {
        listOf(takeInsideCurlyBraces(lastPart)) + getQueryParamsNames(takeAfterCurlyBraces(lastPart))
    } else {
        emptyList()
    }

/** @return inferred argument by [defaultValue]. */
internal fun inferArgument(name: String, defaultValue: String, rFilePackage: String): DestArgument =
    parseReference(defaultValue, rFilePackage)?.let { reference -> DestArgument(name, ReferenceType, ReferenceValue(reference)) }
        ?: parseLongValue(defaultValue)?.let { longValue -> DestArgument(name, LongType, longValue) }
        ?: parseIntValue(defaultValue)?.let { intValue -> DestArgument(name, IntType, intValue) }
        ?: parseFloatValue(defaultValue)?.let { floatValue -> DestArgument(name, FloatType, floatValue) }
        ?: parseBoolean(defaultValue)?.let { boolValue -> DestArgument(name, BoolType, boolValue) }
        ?: DestArgument(name, StringType, StringValue(defaultValue))

internal fun parseIntValue(value: String): IntValue? {
    try {
        if (value.startsWith("0x")) {
            Integer.parseUnsignedInt(value.substring(2), 16)
        } else {
            Integer.parseInt(value)
        }
    } catch (ex: NumberFormatException) {
        return null
    }
    return IntValue(value)
}

internal fun parseLongValue(value: String): LongValue? {
    if (!value.endsWith('L')) {
        return null
    }
    try {
        val normalizedValue = value.substringBeforeLast('L')
        if (normalizedValue.startsWith("0x")) {
            normalizedValue.substring(2).toLong(16)
        } else {
            normalizedValue.toLong()
        }
    } catch (ex: NumberFormatException) {
        return null
    }
    return LongValue(value)
}

private fun parseFloatValue(value: String): FloatValue? =
    value.toFloatOrNull()?.let { FloatValue(value) }

private fun parseBoolean(value: String): BooleanValue? {
    if (value == VALUE_TRUE || value == VALUE_FALSE) {
        return BooleanValue(value)
    }
    return null
}
