package family.amma.deep_link.generator.ext

import org.xmlpull.v1.XmlPullParser

/** @return the value of the attribute with the specified namespace and name, if it exists, or null. */
fun XmlPullParser.attrValue(namespace: String, name: String): String? =
    (0 until attributeCount)
        .find { namespace == getAttributeNamespace(it) && name == getAttributeName(it) }
        ?.let(::getAttributeValue)

/**
 * Loops through the xml file until the next start tag.
 * @param stopTraverse Called when a start tag is encountered. If true, the loop will be stopped.
 */
inline fun XmlPullParser.traverseStartTags(
    stopTraverse: () -> Boolean,
    cacheData: (lineNumber: Int, columnNumber: Int) -> Unit
) {
    while (eventType != XmlPullParser.END_DOCUMENT) {
        val processedLine = lineNumber
        val processedColumn = columnNumber
        if (eventType == XmlPullParser.START_TAG) {
            if (stopTraverse()) return
        }

        // otherwise onStart already called next() and we need to try to process current node
        if (processedLine == lineNumber && processedColumn == columnNumber) {
            cacheData(processedLine, processedColumn)
            nextToken()
        }
    }
}
