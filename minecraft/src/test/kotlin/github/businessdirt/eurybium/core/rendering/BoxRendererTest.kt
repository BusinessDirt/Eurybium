package github.businessdirt.eurybium.core.rendering

import gg.essential.universal.UMatrixStack
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import java.awt.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BoxRendererTest {

    private val box = AABB(10.0, 20.0, 30.0, 12.0, 23.0, 34.0)
    private val color = Color(10, 20, 30, 40)

    @Test
    fun `filled box has six outward faces and eight shared corners`() {
        val recorder = VertexRecorder()
        BoxRenderer.writeFilledBox(recorder.consumer, UMatrixStack(), box, color)
        assertEquals(24, recorder.vertices.size)
        assertTrue(recorder.vertices.all { it.color == color })
        val positions = recorder.vertices.map { it.position }
        assertEquals(8, positions.toSet().size)
        assertTrue(positions.groupingBy { it }.eachCount().values.all { it == 3 })
        val normals = positions.chunked(4).map { face ->
            val normal = face[1].subtract(face[0]).cross(face[2].subtract(face[0])).normalize()
            assertTrue(normal.dot(face[0].subtract(box.center)) > 0.0)
            assertTrue(face.all { normal.dot(it.subtract(face[0])) == 0.0 })
            normal
        }
        assertEquals(6, normals.toSet().size)
    }

    @Test
    fun `outline contains exactly twelve distinct cube edges in a single vertex batch`() {
        val recorder = VertexRecorder()
        BoxRenderer.writeOutlinedBox(
            recorder.consumer, UMatrixStack(), box, Vec3(0.0, 0.0, 0.0), color, 2f,
        )
        assertEquals(48, recorder.vertices.size)
        assertTrue(recorder.vertices.all { it.color == color })
        val edges = recorder.vertices.chunked(4).map { quad ->
            val start = quad[0].position.add(quad[1].position).scale(0.5)
            val end = quad[2].position.add(quad[3].position).scale(0.5)
            assertEquals(1, listOf(start.x != end.x, start.y != end.y, start.z != end.z).count { it })
            assertEquals(0.008, quad[0].position.distanceTo(quad[1].position), 1e-10)
            setOf(start, end)
        }
        assertEquals(12, edges.toSet().size)
        assertEquals(8, edges.flatten().toSet().size)
    }

    @Test
    fun `transparent boxes and invalid outline widths emit no vertices`() {
        val recorder = VertexRecorder()
        val transparent = Color(0, 0, 0, 0)
        BoxRenderer.writeFilledBox(recorder.consumer, UMatrixStack(), box, transparent)
        BoxRenderer.writeOutlinedBox(recorder.consumer, UMatrixStack(), box, Vec3.ZERO, transparent, 2f)
        for (width in listOf(0f, -1f, Float.NaN, Float.POSITIVE_INFINITY)) {
            BoxRenderer.writeOutlinedBox(recorder.consumer, UMatrixStack(), box, Vec3.ZERO, color, width)
        }
        assertTrue(recorder.vertices.isEmpty())
    }
}
