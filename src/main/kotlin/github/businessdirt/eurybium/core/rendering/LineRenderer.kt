package github.businessdirt.eurybium.core.rendering

import com.mojang.blaze3d.systems.RenderSystem
import gg.essential.universal.UGraphics
import gg.essential.universal.UMatrixStack
import gg.essential.universal.render.URenderPipeline
import gg.essential.universal.shader.BlendState
import gg.essential.universal.vertex.UBufferBuilder
import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.events.minecraft.rendering.WorldRenderAfterEntitiesEvent
import github.businessdirt.eurybium.utils.Reference
import net.minecraft.util.math.Vec3d
import java.awt.Color

object LineRenderer {

    private val linesPipeline = URenderPipeline.builderWithDefaultShader("${Reference.MOD_ID}:pipeline/lines",
        UGraphics.DrawMode.LINES, UGraphics.CommonVertexFormats.POSITION_COLOR
    ).apply {
        blendState = BlendState.ALPHA
    }.build()

    private val noDepthLinesPipeline = URenderPipeline.builderWithDefaultShader("${Reference.MOD_ID}:pipeline/no_depth_lines",
        UGraphics.DrawMode.LINES, UGraphics.CommonVertexFormats.POSITION_COLOR
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
        width: Float = 2f
    ) {
        matrices.push()
        CameraTransform.setup(matrices)
        RenderSystem.lineWidth(width)

        val buffer = UBufferBuilder.create(UGraphics.DrawMode.LINE_STRIP, UGraphics.CommonVertexFormats.POSITION_COLOR)
        buffer.pos(matrices, from.x, from.y, from.z).color(color).endVertex()
        buffer.pos(matrices, to.x, to.y, to.z).color(color).endVertex()
        buffer.build()?.drawAndClose(linesPipeline)

        matrices.pop()
    }

    @HandleEvent
    fun test(event: WorldRenderAfterEntitiesEvent) {
        RenderState.enableCull()

        val from: Vec3d = Vec3d(1.0, 148.0, 238.0)
        val to: Vec3d = Vec3d(2.0, 149.0, 239.0)
        draw3DLine(event.matrixStack, from, to)

        RenderState.disableCull()
    }
}