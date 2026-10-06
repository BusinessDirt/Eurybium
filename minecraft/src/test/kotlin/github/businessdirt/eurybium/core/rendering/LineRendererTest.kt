package github.businessdirt.eurybium.core.rendering

import gg.essential.universal.UMatrixStack
import gg.essential.universal.render.URenderPipeline
import gg.essential.universal.shader.BlendState
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import java.awt.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LineRendererTest {

    @Test
    fun `quad faces the camera and preserves endpoints and legacy width`() {
        val from = Vec3(1.0, 2.0, 3.0)
        val to = Vec3(5.0, 6.0, 7.0)
        val camera = Vec3(10.0, 20.0, 30.0)
        val quad = LineRenderer.lineToQuad(from, to, camera, 2f)
        assertEquals(4, quad.size)
        assertTrue(quad[0].add(quad[1]).scale(0.5).distanceTo(from) < 1e-10)
        assertTrue(quad[2].add(quad[3]).scale(0.5).distanceTo(to) < 1e-10)
        val side = quad[1].subtract(quad[0])
        assertEquals(0.008, side.length(), 1e-10)
        assertEquals(0.0, side.dot(to.subtract(from)), 1e-10)
        assertEquals(0.0, side.dot(camera.subtract(from.add(to).scale(0.5))), 1e-10)
    }

    @Test
    fun `vertical parallel and midpoint views have finite fallback geometry`() {
        val from = Vec3.ZERO
        for (to in listOf(Vec3(0.0, 1.0, 0.0), Vec3(1.0, 0.0, 0.0))) {
            for (camera in listOf(to.scale(2.0), to.scale(0.5))) {
                val quad = LineRenderer.lineToQuad(from, to, camera, 2f)
                assertEquals(4, quad.size)
                assertTrue(quad.all { it.isFinite() })
                assertEquals(0.008, quad[0].distanceTo(quad[1]), 1e-10)
            }
        }
    }

    @Test
    fun `invalid or degenerate lines emit no geometry`() {
        val to = Vec3(1.0, 0.0, 0.0)
        for (width in listOf(0f, -1f, Float.NaN, Float.POSITIVE_INFINITY)) {
            assertTrue(LineRenderer.lineToQuad(Vec3.ZERO, to, to, width).isEmpty())
        }
        assertTrue(LineRenderer.lineToQuad(to, to, to, 2f).isEmpty())
        val invalid = Vec3(Double.NaN, 0.0, 0.0)
        assertTrue(LineRenderer.lineToQuad(invalid, to, to, 2f).isEmpty())
        assertTrue(LineRenderer.lineToQuad(Vec3.ZERO, to, invalid, 2f).isEmpty())
        assertTrue(LineRenderer.lineToQuad(Vec3.ZERO, Vec3(Double.MAX_VALUE, 0.0, 0.0), to, 2f).isEmpty())
    }

    @Test
    fun `distant thin lines retain width after transformation without changing the pose`() {
        val matrices = UMatrixStack().apply { translate(-1_000_000f, 0f, 0f) }
        val original = Matrix4f(matrices.peek().model)
        val recorder = VertexRecorder()
        val color = Color(10, 20, 30, 40)
        LineRenderer.writeLine(
            recorder.consumer, matrices,
            Vec3(1_000_000.0, 0.0, 0.0), Vec3(1_000_000.0, 1.0, 0.0),
            Vec3(1_000_000.0, 0.0, 10.0), color, 2f,
        )
        assertEquals(4, recorder.vertices.size)
        val a = recorder.vertices[0].position.x.toFloat()
        val b = recorder.vertices[1].position.x.toFloat()
        assertEquals(0.008f, b - a, 1e-8f)
        assertTrue(recorder.vertices.all { it.color == color })
        assertEquals(original, matrices.peek().model)
    }

    @Test
    fun `vertex transformation includes each affine axis and translation exactly once`() {
        val transform = Matrix4f()
            .m00(2f).m01(3f).m02(4f)
            .m10(5f).m11(6f).m12(7f)
            .m20(8f).m21(9f).m22(10f)
            .m30(11f).m31(12f).m32(13f)
        val original = Matrix4f(transform)
        val recorder = VertexRecorder()
        LineRenderer.writeVertex(recorder.consumer, transform, 1.0, 2.0, 3.0, Color.WHITE)
        assertEquals(Vec3(47.0, 54.0, 61.0), recorder.vertices.single().position)
        assertEquals(original, transform)
    }

    @Test
    fun `transparent lines emit no vertices`() {
        val recorder = VertexRecorder()
        LineRenderer.writeLine(
            recorder.consumer, UMatrixStack(), Vec3.ZERO, Vec3(1.0, 0.0, 0.0),
            Vec3(0.0, 0.0, 10.0), Color(0, 0, 0, 0), 2f,
        )
        assertTrue(recorder.vertices.isEmpty())
    }

    @Test
    fun `both renderers explicitly configure occlusion and never write depth`() {
        for (depth in listOf(false, true)) {
            for (builder in listOf(LineRenderer.pipelineBuilder(depth), BoxRenderer.pipelineBuilder(depth))) {
                assertEquals(if (depth) URenderPipeline.DepthTest.LessOrEqual else URenderPipeline.DepthTest.Always, builder.depthTest)
                assertFalse(builder.depthMask)
                assertFalse(builder.culling)
                assertEquals(BlendState.ALPHA, builder.blendState)
            }
        }
    }
}
