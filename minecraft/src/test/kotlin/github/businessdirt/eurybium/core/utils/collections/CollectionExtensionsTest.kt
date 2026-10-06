package github.businessdirt.eurybium.core.utils.collections

import github.businessdirt.eurybium.core.utils.collections.CollectionExtensions.drainTo
import org.junit.jupiter.api.Test
import java.util.ArrayDeque
import kotlin.test.*

class CollectionExtensionsTest {

    @Test
    fun `drain transfers entries in order and returns the same destination`() {
        val queue = ArrayDeque(listOf(1, 2, 3))
        val destination = mutableListOf<Number>(0)
        assertSame(destination, queue.drainTo(destination))
        assertEquals(listOf<Number>(0, 1, 2, 3), destination)
        assertTrue(queue.isEmpty())
        queue.drainTo(destination)
        assertEquals(4, destination.size)
    }
}
