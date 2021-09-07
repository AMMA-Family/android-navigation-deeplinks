package family.amma.deep_link.generator.main

import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import family.amma.deep_link.generator.entity.Format
import family.amma.deep_link.generator.entity.LiteralsFormat
import family.amma.deep_link.generator.entity.StringFormat

internal sealed interface NavType {
    val typeName: TypeName
    val allowsNullable: Boolean
    val format: Format

    companion object {
        private fun types(): List<NavType> =
            listOf(IntType, LongType, FloatType, BoolType, ReferenceType, StringType)

        fun from(name: String?) =
            name?.let { types().firstOrNull { it.toString() == name } } ?: StringType
    }
}

internal val NavType.list: ParameterizedTypeName get() = List::class.asTypeName().parameterizedBy(typeName)
internal val NavType.nullable: TypeName get() = typeName.copy(nullable = true)

internal object IntType : NavType {
    override val typeName: TypeName = Int::class.asTypeName()
    override fun toString() = "integer"
    override val allowsNullable = false
    override val format: Format = LiteralsFormat
}

internal object LongType : NavType {
    override val typeName: TypeName = Long::class.asTypeName()
    override fun toString() = "long"
    override val allowsNullable = false
    override val format: Format = LiteralsFormat
}

internal object FloatType : NavType {
    override val typeName: TypeName = Float::class.asTypeName()
    override fun toString() = "float"
    override val allowsNullable = false
    override val format: Format = LiteralsFormat
}

internal object StringType : NavType {
    override val typeName: TypeName = String::class.asTypeName()
    override fun toString() = "string"
    override val allowsNullable = true
    override val format: Format = StringFormat
}

internal object BoolType : NavType {
    override val typeName: TypeName = Boolean::class.asTypeName()
    override fun toString() = "boolean"
    override val allowsNullable = false
    override val format: Format = LiteralsFormat
}

internal object ReferenceType : NavType {
    // it is internally the same as INT, but we don't want to allow to
    // assignment between int and reference args
    override val typeName: TypeName = Int::class.asTypeName()
    override fun toString() = "reference"
    override val allowsNullable = false
    override val format: Format = StringFormat
}
