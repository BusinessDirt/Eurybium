package github.businessdirt.eurybium.core.rendering

import gg.essential.universal.UGraphics
import gg.essential.universal.UMatrixStack
import gg.essential.universal.UMinecraft.getMinecraft
import gg.essential.universal.render.URenderPipeline
import gg.essential.universal.shader.BlendState
import gg.essential.universal.vertex.UBufferBuilder
import gg.essential.universal.vertex.UVertexConsumer
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4fc
import java.awt.Color
import kotlin.math.abs
import kotlin.math.sqrt

/** World-space line overlays rendered as camera-facing, two-sided quads on the render thread. */
object LineRenderer {

    private const val MIN_LENGTH_SQUARED = 1e-12
    private const val WIDTH_SCALE = 500.0

    private val linesPipeline by lazy { pipelineBuilder(depth = true).build() }
    private val noDepthLinesPipeline by lazy { pipelineBuilder(depth = false).build() }

    /**
     * Draws one line without changing [matrices]. They must already include the world-to-camera transform.
     *
     * [color] includes alpha. [width] retains the legacy scale: full thickness is width / 250 blocks,
     * rather than a fixed pixel width, so distant lines appear thinner. [depth] enables terrain occlusion.
     * Transparent, non-finite, non-positive-width, and effectively zero-length lines are skipped.
     */
    fun draw3DLine(
        matrices: UMatrixStack,
        from: Vec3,
        to: Vec3,
        color: Color = Color.WHITE,
        width: Float = 2f,
        depth: Boolean = false,
    ) {
        if (color.alpha == 0 || !validLine(from, to, width)) return

        val cameraPosition = getMinecraft().gameRenderer.mainCamera.position()
        val vertices = lineToQuad(from, to, cameraPosition, width)
        if (vertices.isEmpty()) return

        val buffer = UBufferBuilder.create(UGraphics.DrawMode.QUADS, UGraphics.CommonVertexFormats.POSITION_COLOR)
        writeVertices(buffer, matrices, vertices, color)
        drawBuffer(buffer, depth)
    }

    /**
     * Builds a camera-facing ribbon in world coordinates, or an empty list for invalid/degenerate input.
     *
     * Its side vector is perpendicular to both the segment and the view toward its midpoint.
     * Looking exactly along a segment has no unique facing plane; a stable world axis supplies the fallback.
     */
    fun lineToQuad(from: Vec3, to: Vec3, cameraPosition: Vec3, width: Float): List<Vec3> {
        if (!validLine(from, to, width) || !cameraPosition.isFinite()) return emptyList()

        val delta = to.subtract(from)
        val direction = delta.scale(1.0 / sqrt(delta.lengthSqr()))
        val midpoint = from.add(delta.scale(0.5))
        val view = cameraPosition.subtract(midpoint)
        val viewLengthSquared = view.lengthSqr()
        var side = if (viewLengthSquared.isFinite() && viewLengthSquared > MIN_LENGTH_SQUARED) {
            direction.cross(view.scale(1.0 / sqrt(viewLengthSquared)))
        } else {
            Vec3.ZERO
        }

        // Parallel view/segment vectors produce a zero cross product. Pick an axis that is
        // not parallel to the segment so vertical and end-on lines still have finite geometry.
        if (side.lengthSqr() <= MIN_LENGTH_SQUARED) {
            val axis = if (abs(direction.y) < 0.99) Vec3(0.0, 1.0, 0.0) else Vec3(1.0, 0.0, 0.0)
            side = direction.cross(axis)
        }

        val offset = side.scale(width.toDouble() / WIDTH_SCALE / sqrt(side.lengthSqr()))

        return listOf(
            from.subtract(offset),
            from.add(offset),
            to.add(offset),
            to.subtract(offset),
        )
    }

    /** Appends a line to an existing quad buffer, allowing all box edges to share one submission. */
    internal fun writeLine(
        buffer: UVertexConsumer,
        matrices: UMatrixStack,
        from: Vec3,
        to: Vec3,
        cameraPosition: Vec3,
        color: Color,
        width: Float,
    ) {
        if (color.alpha == 0) return
        writeVertices(buffer, matrices, lineToQuad(from, to, cameraPosition, width), color)
    }

    /** Draws and releases a complete line batch, including release if drawing fails. */
    internal fun drawBuffer(buffer: UBufferBuilder, depth: Boolean) {
        buffer.build()?.drawAndClose(if (depth) linesPipeline else noDepthLinesPipeline)
    }

    /** Explicit overlay state: terrain testing is optional, depth-buffer writes are always disabled. */
    internal fun pipelineBuilder(depth: Boolean): URenderPipeline.Builder =
        URenderPipeline.builderWithDefaultShader(
            if (depth) "eurybium:pipeline/lines" else "eurybium:pipeline/no_depth_lines",
            UGraphics.DrawMode.QUADS,
            UGraphics.CommonVertexFormats.POSITION_COLOR,
        ).apply {
            blendState = BlendState.ALPHA
            depthTest = if (depth) URenderPipeline.DepthTest.LessOrEqual else URenderPipeline.DepthTest.Always
            depthMask = false
            culling = false
        }

    private fun writeVertices(buffer: UVertexConsumer, matrices: UMatrixStack, vertices: List<Vec3>, color: Color) {
        val transform = matrices.peek().model

        for (vertex in vertices) {
            writeVertex(buffer, transform, vertex.x, vertex.y, vertex.z, color)
        }
    }

    /** Applies the pose before float conversion so small details survive large world coordinates. */
    internal fun writeVertex(
        buffer: UVertexConsumer,
        transform: Matrix4fc,
        x: Double,
        y: Double,
        z: Double,
        color: Color,
    ) {
        // UniversalCraft's pos(matrices, ...) casts the original coordinates to floats first.
        // Transform in doubles and use UNIT to avoid both that precision loss and a second transform.
        buffer.pos(
            UMatrixStack.UNIT,
            transform.m00() * x + transform.m10() * y + transform.m20() * z + transform.m30(),
            transform.m01() * x + transform.m11() * y + transform.m21() * z + transform.m31(),
            transform.m02() * x + transform.m12() * y + transform.m22() * z + transform.m32(),
        ).color(color).endVertex()
    }

    private fun validLine(from: Vec3, to: Vec3, width: Float): Boolean {
        if (!width.isFinite() || width <= 0 || !from.isFinite() || !to.isFinite()) return false

        val lengthSquared = to.subtract(from).lengthSqr()
        return lengthSquared.isFinite() && lengthSquared > MIN_LENGTH_SQUARED
    }

}
