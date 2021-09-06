package family.amma.deep_link.generator.main

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import family.amma.deep_link.generator.entity.ResReference

internal sealed class NavType {
    abstract val typeName: TypeName
    abstract val allowsNullable: Boolean
    abstract val format: String

    companion object {
        private fun types(): List<NavType> =
            listOf(IntType, LongType, FloatType, BoolType, ReferenceType, StringType)

        fun from(name: String?) =
            name?.let { types().firstOrNull { it.toString() == name } } ?: StringType
    }
}

internal val NavType.list: ParameterizedTypeName get() = List::class.asTypeName().parameterizedBy(typeName)
internal val NavType.nullable: TypeName get() = typeName.copy(nullable = true)

internal object IntType : NavType() {
    override val typeName: TypeName = Int::class.asTypeName()
    override fun toString() = "integer"
    override val allowsNullable = false
    override val format: String = "%L"
}

internal object LongType : NavType() {
    override val typeName: TypeName = Long::class.asTypeName()
    override fun toString() = "long"
    override val allowsNullable = false
    override val format: String = "%L"
}

internal object FloatType : NavType() {
    override val typeName: TypeName = Float::class.asTypeName()
    override fun toString() = "float"
    override val allowsNullable = false
    override val format: String = "%L"
}

internal object StringType : NavType() {
    override val typeName: TypeName = String::class.asTypeName()
    override fun toString() = "string"
    override val allowsNullable = true
    override val format: String = "%S"
}

internal object BoolType : NavType() {
    override val typeName: TypeName = Boolean::class.asTypeName()
    override fun toString() = "boolean"
    override val allowsNullable = false
    override val format: String = "%L"
}

internal object ReferenceType : NavType() {
    // it is internally the same as INT, but we don't want to allow to
    // assignment between int and reference args
    override val typeName: TypeName = Int::class.asTypeName()
    override fun toString() = "reference"
    override val allowsNullable = false
    override val format: String = "%S"
}

internal sealed class WriteableValue {
    abstract fun write(): CodeBlock
}

internal data class ReferenceValue(private val resReference: ResReference) : WriteableValue() {
    override fun write(): CodeBlock = CodeBlock.of(resReference.accessor)
}

internal data class StringValue(private val value: String) : WriteableValue() {
    override fun write(): CodeBlock = CodeBlock.of(StringType.format, value)
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
