package family.amma.deep_link.generator.ext

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import family.amma.deep_link.generator.entity.GenerateProp
import family.amma.deep_link.generator.parser.LITERALS_FORMAT
import family.amma.deep_link.generator.parser.STRING_FORMAT
import org.junit.Test
import kotlin.test.assertEquals

class KotlinPoetTest {
    @Test
    fun testAddConstructorWithProp() {
        assertEquals(
            expected = """
                public class Foo(
                  private val bar: kotlin.String
                )
            """.trimIndent(),
            actual = TypeSpec
                .classBuilder("Foo")
                .addConstructorWithProp<String>("bar", KModifier.PRIVATE)
                .build()
                .toString().trim()
        )
        assertEquals(
            expected = """
                public class Clazz(
                  public val foo: kotlin.Int = 10,
                  public val bar: kotlin.String? = null
                )
            """.trimIndent(),
            actual = TypeSpec
                .classBuilder("Clazz")
                .addConstructorWithProps(
                    listOf(
                        GenerateProp(name = "foo", typeName = Int::class.asTypeName(), defaultValue = CodeBlock.of("10")),
                        GenerateProp(
                            name = "bar",
                            typeName = String::class.asTypeName().copy(nullable = true),
                            defaultValue = CodeBlock.of("null")
                        )
                    )
                )
                .build()
                .toString().trim()
        )
    }

    @Test
    fun testToCodeBlock() {
        assertEquals(
            expected = "listOf(\"lol\", \"kek\", \"azaza\")",
            actual = toListCodeBlock("%S", listOf("lol", "kek", "azaza")).toString().trim()
        )
        assertEquals(
            expected = "listOf(\"1\", \"2\", \"3\")",
            actual = toListCodeBlock("%S", listOf(1, 2, 3)).toString().trim()
        )
    }

    @Test
    fun testConstValProp() {
        assertEquals(
            expected = "const val name: kotlin.String = \"fpp\"",
            actual = constValProp(name = "name", typeToFormat = String::class to STRING_FORMAT, value = "fpp").toString().trim()
        )
        assertEquals(
            expected = "const val age: kotlin.Int = 100",
            actual = constValProp(name = "age", typeToFormat = Int::class to LITERALS_FORMAT, value = "100").toString().trim()
        )
        assertEquals(
            expected = "const val isOk: kotlin.Boolean = false",
            actual = constValProp(name = "isOk", typeToFormat = Boolean::class to LITERALS_FORMAT, value = "false").toString().trim()
        )
    }

    @Test
    fun testListProp() {
        assertEquals(
            expected = "val userIds: kotlin.collections.List<kotlin.String> = listOf(1, 2, 3)",
            actual = listProp(name = "userIds", value = toListCodeBlock(LITERALS_FORMAT, list = listOf(1, 2, 3))).toString().trim()
        )
    }
}