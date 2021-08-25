package family.amma.deep_link.generator.fileSpec.hierarchy

import family.amma.deep_link.generator.entity.DeepLink
import family.amma.deep_link.generator.fileSpec.filterLastLevelDeepLinks
import io.mockk.every
import io.mockk.mockkClass
import kotlin.test.Test
import kotlin.test.assertEquals

class HierarchyTest {
    @Test
    fun filterLastLevelDeepLinksTest() {
        val deepLink1 = mockkClass(DeepLink::class) {
            every { parsedUri } returns listOf("foo", "bar")
        }
        val deepLink2 = mockkClass(DeepLink::class) {
            every { parsedUri } returns listOf("foo", "bar", "zoo")
        }
        val deepLink3 = mockkClass(DeepLink::class) {
            every { parsedUri } returns listOf("foo", "bar", "qwerty")
        }
        val deepLinksList = listOf(deepLink1, deepLink2, deepLink3)
        assertEquals(expected = listOf(), actual = deepLinksList.filterLastLevelDeepLinks(currentSegment = 0))
        assertEquals(expected = listOf(deepLink1), actual = deepLinksList.filterLastLevelDeepLinks(currentSegment = 1))
        assertEquals(expected = listOf(deepLink2, deepLink3), actual = deepLinksList.filterLastLevelDeepLinks(currentSegment = 2))
    }
}
