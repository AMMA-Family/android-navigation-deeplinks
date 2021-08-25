package family.amma.deep_link.generator.entity

import kotlin.test.Test
import kotlin.test.assertEquals

class ResReferenceTest {
    @Test
    fun testResReference() {
        val resReference = ResReference(packageName = "package", resType = "id", name = "foo.bar.zoo")
        assertEquals(expected = true, actual = resReference.isId())
        assertEquals(expected = "foo_bar_zoo", actual = resReference.identifier)
        assertEquals(expected = "package.R.id.foo_bar_zoo", actual = resReference.accessor)
    }
}