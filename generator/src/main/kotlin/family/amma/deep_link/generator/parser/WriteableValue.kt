package family.amma.deep_link.generator.parser

import com.squareup.kotlinpoet.CodeBlock
import family.amma.deep_link.generator.entity.ResReference
import family.amma.deep_link.generator.entity.StringFormat

internal sealed interface WriteableValue {
    fun write(): CodeBlock
}

internal data class ReferenceValue(private val resReference: ResReference) : WriteableValue {
    override fun write(): CodeBlock = CodeBlock.of(resReference.accessor)
}

internal data class StringValue(private val value: String) : WriteableValue {
    override fun write(): CodeBlock = CodeBlock.of(StringFormat.value, value)
}

// keeping value as String, it will help to preserve client format of it: hex, dec
internal data class IntValue(private val value: String) : WriteableValue {
    override fun write(): CodeBlock = CodeBlock.of(value)
}

// keeping value as String, it will help to preserve client format of it: hex, dec
internal data class LongValue(private val value: String) : WriteableValue {
    override fun write(): CodeBlock = CodeBlock.of(value)
}

// keeping value as String, it will help to preserve client format of it: scientific, dot
internal data class FloatValue(private val value: String) : WriteableValue {
    override fun write(): CodeBlock = CodeBlock.of("${value}F")
}

internal data class BooleanValue(private val value: String) : WriteableValue {
    override fun write(): CodeBlock = CodeBlock.of(value)
}

internal object NullValue : WriteableValue {
    override fun write(): CodeBlock = CodeBlock.of("null")
}
