package github.businessdirt.eurybium.core.rendering

import gg.essential.universal.UGraphics
import gg.essential.universal.UMatrixStack
import gg.essential.universal.render.URenderPipeline
import gg.essential.universal.shader.BlendState
import gg.essential.universal.vertex.UBufferBuilder
import net.minecraft.world.phys.Vec3
import java.awt.Color

object LineRenderer {

    private val linesPipeline: URenderPipeline by lazy {
        URenderPipeline.builderWithDefaultShader("eurybium:pipeline/lines",
            UGraphics.DrawMode.QUADS, UGraphics.CommonVertexFormats.POSITION_COLOR
        ).apply {
            blendState = BlendState.ALPHA
        }.build()
    }

    private val noDepthLinesPipeline: URenderPipeline by lazy {
        URenderPipeline.builderWithDefaultShader("eurybium:pipeline/no_depth_lines",
            UGraphics.DrawMode.QUADS, UGraphics.CommonVertexFormats.POSITION_COLOR
        ).apply {
            depthTest = URenderPipeline.DepthTest.Always
            blendState = BlendState.ALPHA
            culling = false
        }.build()
    }

    /**
     * Draws a simple 3D line between two points in world space.
     *
     * @param matrices The current UMatrixStack (wraps Fabric/Forge MatrixStack).
     * @param from Triple of (x,y,z) start position in world space.
     * @param to Triple of (x,y,z) end position in world space.
     * @param color RGBA packed as 0xAARRGGBB.
     * @param width OpenGL line width.
     */
    fun draw3DLine(
        matrices: UMatrixStack,
        from: Vec3,
        to: Vec3,
        color: Color = Color.WHITE,
        width: Float = 2f,
        depth: Boolean = false
    ) {
        val cameraDir = (to.subtract(from)).normalize()
        val axis = if (kotlin.math.abs(cameraDir.y) > 0.99) Vec3(1.0, 0.0, 0.0) else Vec3(0.0, 1.0, 0.0)
        val cameraRight = cameraDir.cross(axis).normalize()
        val cameraUp = cameraRight.cross(cameraDir).normalize()

        val buffer = UBufferBuilder.create(UGraphics.DrawMode.QUADS, UGraphics.CommonVertexFormats.POSITION_COLOR)
        lineToQuad(from, to, cameraRight, cameraUp, width).forEach { buffer.pos(matrices, it.x, it.y, it.z).color(color).endVertex() }
        buffer.build()?.drawAndClose(if (depth) linesPipeline else noDepthLinesPipeline)
    }

    fun lineToQuad(from: Vec3, to: Vec3, cameraRight: Vec3, cameraUp: Vec3, width: Float): List<Vec3> {
        val halfWidth = (width / 500).toDouble()
        val rightOffset = cameraRight.scale(halfWidth)
        val upOffset = cameraUp.scale(halfWidth)

        return listOf(
            from.subtract(rightOffset).subtract(upOffset), // bottom-left
            from.add(rightOffset).subtract(upOffset),      // bottom-right
            to.add(rightOffset).add(upOffset),            // top-right
            to.subtract(rightOffset).add(upOffset)        // top-left
        )
    }
}