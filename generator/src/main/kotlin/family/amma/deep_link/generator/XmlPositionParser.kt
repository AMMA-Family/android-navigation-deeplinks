package family.amma.deep_link.generator

import family.amma.deep_link.generator.ext.attrValue
import family.amma.deep_link.generator.ext.traverseStartTags
import family.amma.deep_link.generator.entity.XmlPosition
import family.amma.deep_link.generator.ext.showError
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

    /** @see XmlPullParser.traverseStartTags */
    inline fun traverseStartTags(stopTraverse: () -> Boolean = { true }) {
        parser.traverseStartTags(stopTraverse = stopTraverse, cacheData = ::cacheData)
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
    inline fun traverseInnerStartTags(onStartTag: () -> Unit = {}) {
        val innerDepth = parser.depth + 1
        cacheData(lineNumber = parser.lineNumber, columnNumber = parser.columnNumber)
        parser.nextToken()
        traverseStartTags(stopTraverse = {
            val currentDepth = parser.depth
            if (innerDepth == currentDepth) {
                onStartTag()
            }
            currentDepth < innerDepth
        })
    }

    /** @see XmlPullParser.attrValue */
    fun attrValue(namespace: String, name: String): String? =
        parser.attrValue(namespace, name)

    /**
     * An error is logged if the specified attribute does not exist.
     * @see XmlPullParser.attrValue
     */
    fun attrValueOrError(namespace: String, attrName: String): String =
        attrValue(namespace, attrName)
            ?: showError(message = mandatoryAttrMissingError(tag(), attrName), position = xmlPosition())
}

private fun mandatoryAttrMissingError(tag: String, attr: String): String =
    "Mandatory attribute '$attr' for tag '$tag' is missing."
