package family.amma.deep_link.generator.entity

/**
 * @param name The name to the file.
 * @param line The line where the error is located.
 * @param column The column where the error is located.
 */
internal data class XmlPosition(val name: String, val line: Int, val column: Int)
