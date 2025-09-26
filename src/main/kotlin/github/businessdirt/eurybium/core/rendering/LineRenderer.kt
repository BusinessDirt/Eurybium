package github.businessdirt.eurybium.core.rendering

import com.mojang.blaze3d.systems.RenderSystem
import gg.essential.universal.UGraphics
import gg.essential.universal.UMatrixStack
import gg.essential.universal.UMinecraft.getMinecraft
import gg.essential.universal.render.URenderPipeline
import gg.essential.universal.shader.BlendState
import gg.essential.universal.vertex.UBufferBuilder
import github.businessdirt.eurybium.utils.Reference
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.Tessellator
import net.minecraft.util.math.Vec3d
import java.awt.Color

object LineRenderer {

    private val linesPipeline = URenderPipeline.builderWithDefaultShader("${Reference.MOD_ID}:pipeline/lines",
        UGraphics.DrawMode.QUADS, UGraphics.CommonVertexFormats.POSITION_COLOR
    ).apply {
        blendState = BlendState.ALPHA
    }.build()

    private val noDepthLinesPipeline = URenderPipeline.builderWithDefaultShader("${Reference.MOD_ID}:pipeline/no_depth_lines",
        UGraphics.DrawMode.QUADS, UGraphics.CommonVertexFormats.POSITION_COLOR
    ).apply {
        depthTest = URenderPipeline.DepthTest.Always
        blendState = BlendState.ALPHA
        culling = false
    }.build()

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
        from: Vec3d,
        to: Vec3d,
        color: Color = Color.WHITE,
        width: Float = 2f,
        depth: Boolean = false
    ) {
        val cameraDir = (to.subtract(from)).normalize()
        val cameraRight = cameraDir.crossProduct(Vec3d(0.0, 1.0, 0.0)).normalize()
        val cameraUp = cameraRight.crossProduct(cameraDir).normalize()

        val buffer = UBufferBuilder.create(UGraphics.DrawMode.QUADS, UGraphics.CommonVertexFormats.POSITION_COLOR)
        lineToQuad(from, to, cameraRight, cameraUp, width).forEach { buffer.pos(matrices, it.x, it.y, it.z).color(color).endVertex() }
        buffer.build()?.drawAndClose(if (depth) linesPipeline else noDepthLinesPipeline)
    }

    fun lineToQuad(from: Vec3d, to: Vec3d, cameraRight: Vec3d, cameraUp: Vec3d, width: Float): List<Vec3d> {
        val halfWidth = (width / 500).toDouble()
        val rightOffset = cameraRight.multiply(halfWidth)
        val upOffset = cameraUp.multiply(halfWidth)

        return listOf(
            from.subtract(rightOffset).subtract(upOffset), // bottom-left
            from.add(rightOffset).subtract(upOffset),      // bottom-right
            to.add(rightOffset).add(upOffset),            // top-right
            to.subtract(rightOffset).add(upOffset)        // top-left
        )
    }
}