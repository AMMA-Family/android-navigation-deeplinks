package family.amma.deep_link.generator.ext

import family.amma.deep_link.generator.entity.XmlPosition
import java.io.File

internal fun showError(message: String, position: XmlPosition): Nothing {
    val path = position.name
    val line = position.line
    val column = position.column
    throw IllegalStateException("\"$path:$line:$column (${File(path).name}:$line): \nError: $message")
}


