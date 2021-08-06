package family.amma.deep_link.generator.ext

import org.junit.Test
import kotlin.test.assertEquals

class ExtensionTest {
    @Test
    fun testFilterUnique() {
        assertEquals(expected = listOf(1, 2), actual = listOf(1, 2, 1).filterUnique())
        assertEquals(expected = listOf("1", "2"), actual = listOf("1", "2", "1").filterUnique())
        assertEquals(expected = listOf(true, false), actual = listOf(true, true, false).filterUnique())
    }

    @Test
    fun testReplace() {
        assertEquals(expected = listOf(0, 1, 2), actual = listOf(1, 1, 2).replace(old = 1, new = 0))
    }

    @Test
    fun testCamelCase() {
        assertEquals(expected = "FooBarZoo", actual = "foo_bar_zoo".toCamelCase())
    }

    @Test
    fun testSnakeCase() {
        assertEquals(expected = "foo_bar_zoo", actual = "FooBarZoo".toSnakeCase())
    }
}