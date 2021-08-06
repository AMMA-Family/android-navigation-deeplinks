package family.amma.deep_link.generator.parser

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import family.amma.deep_link.generator.entity.ResReference

const val STRING_FORMAT = "%S"
const val LITERALS_FORMAT = "%L"

internal sealed class NavType {
    abstract fun typeName(): TypeName
    abstract fun allowsNullable(): Boolean

    companion object {
        fun from(name: String?) = when (name) {
            "integer" -> IntType
            "long" -> LongType
            "float" -> FloatType
            "boolean" -> BoolType
            "reference" -> ReferenceType
            // We cannot process the type, so we expect a string.
            else -> StringType
        }
    }
}

internal object IntType : NavType() {
    override fun typeName(): TypeName = Int::class.asTypeName()
    override fun toString() = "integer"
    override fun allowsNullable() = false
}

internal object LongType : NavType() {
    override fun typeName(): TypeName = Long::class.asTypeName()
    override fun toString() = "long"
    override fun allowsNullable() = false
}

internal object FloatType : NavType() {
    override fun typeName(): TypeName = Float::class.asTypeName()
    override fun toString() = "float"
    override fun allowsNullable() = false
}

internal object StringType : NavType() {
    override fun typeName(): TypeName = String::class.asTypeName()
    override fun toString() = "string"
    override fun allowsNullable() = true
}

internal object BoolType : NavType() {
    override fun typeName(): TypeName = Boolean::class.asTypeName()
    override fun toString() = "boolean"
    override fun allowsNullable() = false
}

internal object ReferenceType : NavType() {
    // it is internally the same as INT, but we don't want to allow to
    // assignment between int and reference args
    override fun typeName(): TypeName = Int::class.asTypeName()
    override fun toString() = "reference"
    override fun allowsNullable() = false
}

internal sealed class WriteableValue {
    abstract fun write(): CodeBlock
}

internal data class ReferenceValue(private val resReference: ResReference) : WriteableValue() {
    override fun write(): CodeBlock = CodeBlock.of(resReference.accessor)
}

internal data class StringValue(private val value: String) : WriteableValue() {
    override fun write(): CodeBlock = CodeBlock.of(STRING_FORMAT, value)
}

// keeping value as String, it will help to preserve client format of it: hex, dec
internal data class IntValue(private val value: String) : WriteableValue() {
    override fun write(): CodeBlock = CodeBlock.of(value)
}

// keeping value as String, it will help to preserve client format of it: hex, dec
internal data class LongValue(private val value: String) : WriteableValue() {
    override fun write(): CodeBlock = CodeBlock.of(value)
}

// keeping value as String, it will help to preserve client format of it: scientific, dot
internal data class FloatValue(private val value: String) : WriteableValue() {
    override fun write(): CodeBlock = CodeBlock.of("${value}F")
}

internal data class BooleanValue(private val value: String) : WriteableValue() {
    override fun write(): CodeBlock = CodeBlock.of(value)
}

internal object NullValue : WriteableValue() {
    override fun write(): CodeBlock = CodeBlock.of("null")
}
