package family.amma.deep_link.generator.entity

import org.junit.Test
import kotlin.test.assertEquals

class DeepLinkTest {
    @Test
    fun testProtocol() {
        assertEquals(
            expected = "http",
            actual = "http://www.example.com/users/{id}?isEditMode={isEditMode}".header().protocol()
        )
        assertEquals(
            expected = null,
            actual = "www.example.com/users/{id}?isEditMode={isEditMode}".header().protocol()
        )
        assertEquals(
            expected = null,
            actual = "foo/bar/zoo".header().protocol()
        )
    }

    @Test
    fun testHost() {
        assertEquals(
            expected = "www.example.com",
            actual = "http://www.example.com/users/{id}?isEditMode={isEditMode}".header().host()
        )
        assertEquals(
            expected = "www.example.com",
            actual = "www.example.com/users/{id}?isEditMode={isEditMode}".header().host()
        )
        assertEquals(
            expected = "foo",
            actual = "foo/bar/zoo".header().host()
        )
    }

    @Test
    fun testTrimPartToParameters() {
        assertEquals(
            expected = "http://www.example.com/users/",
            actual = "http://www.example.com/users/{id}".trimPartToParameters()
        )
    }

    @Test
    fun testPathSegments() {
        assertEquals(
            expected = listOf("users", "a", "b"),
            actual = "http://www.example.com/users/a/b".pathSegments()
        )
    }

    @Test
    fun integrationTest() {
        assertEquals(
            expected = listOf("http", "www.example.com", "users"),
            actual = "http://www.example.com/users/{id}?isEditMode={isEditMode}".parsed()
        )
    }
}