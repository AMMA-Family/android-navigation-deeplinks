package family.amma.deep_link.generator.parser

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.Reader

/**
 * Custom parser.
 * @param fileName Name of parsed file.
 * @param reader Input source.
 */
internal class XmlPositionParser(private val fileName: String, reader: Reader) {
    private val parser: XmlPullParser = XmlPullParserFactory.newInstance().newPullParser().apply {
        setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
        setInput(reader)
    }

    /** @return [XmlPullParser.getName] or empty. */
    fun tag(): String = parser.name ?: ""

    /**
     * Loops through the xml file until the next start tag.
     * @param stopTraverse Called when a start tag is encountered. If true, the loop will be stopped.
     */
    suspend fun traverseStartTags(dispatcher: CoroutineDispatcher, stopTraverse: suspend () -> Boolean = { true }) = with(parser) {
        withContext(dispatcher) {
            while (eventType != XmlPullParser.END_DOCUMENT) {
                val processedLine = lineNumber
                val processedColumn = columnNumber
                if (eventType == XmlPullParser.START_TAG) {
                    if (stopTraverse()) return@withContext
                }

                // otherwise onStart already called next() and we need to try to process current node
                if (processedLine == lineNumber && processedColumn == columnNumber) {
                    cacheData(processedLine, processedColumn)
                    @Suppress("BlockingMethodInNonBlockingContext")
                    nextToken()
                }
            }
        }
    }

    private var startLine = 0
    private var startColumn = 0

    private fun cacheData(lineNumber: Int, columnNumber: Int) {
        startLine = lineNumber
        startColumn = columnNumber
    }

    /** Current position in xml file. */
    fun xmlPosition() = XmlPosition(name = fileName, line = startLine, column = startColumn - 1)

    /**
     * Loops through the xml file until the next start tag at the same depth.
     * @param onStartTag Called when a start tag is encountered at inner depth.
     */
    suspend fun traverseInnerStartTags(dispatcher: CoroutineDispatcher, onStartTag: suspend () -> Unit = {}) = withContext(dispatcher) {
        val innerDepth = parser.depth + 1
        cacheData(lineNumber = parser.lineNumber, columnNumber = parser.columnNumber)
        @Suppress("BlockingMethodInNonBlockingContext")
        parser.nextToken()
        traverseStartTags(dispatcher, stopTraverse = {
            val currentDepth = parser.depth
            if (innerDepth == currentDepth) {
                onStartTag()
            }
            currentDepth < innerDepth
        })
    }

    /** @return the value of the attribute with the specified namespace and name, if it exists, or null. */
    suspend fun attrValue(namespace: String, name: String, dispatcher: CoroutineDispatcher): String? = with(parser) {
        withContext(dispatcher) {
            (0 until attributeCount)
                .find { namespace == getAttributeNamespace(it) && name == getAttributeName(it) }
                ?.let(::getAttributeValue)
        }
    }

    /**
     * An error is logged if the specified attribute does not exist.
     * @see attrValue
     */
    suspend fun attrValueOrError(namespace: String, attrName: String, dispatcher: CoroutineDispatcher): String =
        attrValue(namespace, attrName, dispatcher)
            ?: showError(message = mandatoryAttrMissingError(tag(), attrName), position = xmlPosition())
}

private fun mandatoryAttrMissingError(tag: String, attr: String): String =
    "Mandatory attribute '$attr' for tag '$tag' is missing."
