package family.amma.deep_link.generator.ext

import com.squareup.kotlinpoet.*
import family.amma.deep_link.generator.entity.GenerateProp
import family.amma.deep_link.generator.main.BoolType
import family.amma.deep_link.generator.main.IntType
import family.amma.deep_link.generator.main.StringType
import family.amma.deep_link.generator.main.list
import kotlin.test.Test
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
                        GenerateProp(name = "foo", type = IntType, defaultValue = CodeBlock.of("10")),
                        GenerateProp(name = "bar", type = StringType, defaultValue = CodeBlock.of("null"))
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
            actual = toListCodeBlock(type = StringType, listOf("lol", "kek", "azaza")).toString().trim()
        )
        assertEquals(
            expected = "listOf(\"1\", \"2\", \"3\")",
            actual = toListCodeBlock(type = StringType, listOf(1, 2, 3)).toString().trim()
        )
    }

    @Test
    fun testOverrideValProp() {
        assertEquals(
            expected = "override val name: kotlin.String = \"fpp\"",
            actual = overrideValProp(name = "name", type = StringType, value = "fpp").toString().trim()
        )
        assertEquals(
            expected = "override val age: kotlin.Int = 100",
            actual = overrideValProp(name = "age", type = IntType, value = "100").toString().trim()
        )
        assertEquals(
            expected = "override val isOk: kotlin.Boolean = false",
            actual = overrideValProp(name = "isOk", type = BoolType, value = "false").toString().trim()
        )
    }

    @Test
    fun testConstValProp() {
        assertEquals(
            expected = "const val name: kotlin.String = \"fpp\"",
            actual = constValProp(name = "name", type = StringType, value = "fpp").toString().trim()
        )
        assertEquals(
            expected = "const val age: kotlin.Int = 100",
            actual = constValProp(name = "age", type = IntType, value = "100").toString().trim()
        )
        assertEquals(
            expected = "const val isOk: kotlin.Boolean = false",
            actual = constValProp(name = "isOk", type = BoolType, value = "false").toString().trim()
        )
    }

    @Test
    fun testListProp() {
        assertEquals(
            expected = "val userIds: kotlin.collections.List<kotlin.Int> = listOf(1, 2, 3)",
            actual = listProp(
                name = "userIds", IntType.list, value = toListCodeBlock(IntType, list = listOf(1, 2, 3))
            ).toString().trim()
        )
    }

    @Test
    fun deepLinkSealedClass() {
        val fooBarClassName = ClassName("packg", "FooBar")
        assertEquals(
            expected = "public sealed class FooBar : deep_link.GeneratedDeepLink",
            actual = deepLinkSealedClass(fooBarClassName, parentClass = null).toString().trim()
        )
        assertEquals(
            expected = """
                public sealed class FooBar : another.packg.ZOO() {
                  public class F : packg.FooBar()
                }
            """.trimIndent(),
            actual = deepLinkSealedClass(
                className = fooBarClassName,
                parentClass = ClassName("another.packg", "ZOO"),
                additional = {
                    addType(TypeSpec.classBuilder("F").superclass(fooBarClassName).build())
                }
            ).toString().trim()
        )
    }
}
