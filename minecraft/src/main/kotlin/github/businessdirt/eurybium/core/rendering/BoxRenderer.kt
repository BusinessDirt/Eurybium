package github.businessdirt.eurybium.core.rendering

import gg.essential.universal.UGraphics
import gg.essential.universal.UMatrixStack
import gg.essential.universal.UMinecraft.getMinecraft
import gg.essential.universal.render.URenderPipeline
import gg.essential.universal.shader.BlendState
import gg.essential.universal.vertex.UBufferBuilder
import gg.essential.universal.vertex.UVertexConsumer
import io.github.notenoughupdates.moulconfig.ChromaColour
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import java.awt.Color

/** Filled and outlined unit-block overlays, drawn on the render thread without modifying the supplied matrices. */
object BoxRenderer {

    // Corner bits select the maximum X, Y, and Z coordinate respectively (1, 2, and 4).
    // Fixed topology is shared by every box instead of rebuilding edge pairs each frame.
    private val edges = intArrayOf(
        0, 1, 2, 3, 4, 5, 6, 7, // X edges
        0, 2, 1, 3, 4, 6, 5, 7, // Y edges
        0, 4, 1, 5, 2, 6, 3, 7, // Z edges
    )

    private val faces = intArrayOf(
        0, 1, 5, 4, // Bottom (-Y)
        2, 6, 7, 3, // Top (+Y)
        0, 2, 3, 1, // North (-Z)
        4, 5, 7, 6, // South (+Z)
        0, 4, 6, 2, // West (-X)
        1, 3, 7, 5, // East (+X)
    )

    /** Alpha-blended fill pipeline that tests against existing terrain depth without writing depth. */
    val boxPipeline: URenderPipeline by lazy { pipelineBuilder(depth = true).build() }

    /** Alpha-blended fill pipeline visible through terrain, without writing depth. */
    val noDepthBoxPipeline: URenderPipeline by lazy { pipelineBuilder(depth = false).build() }

    /**
     * Draws all twelve edges in one buffer submission, using the same camera-facing width as [LineRenderer].
     * [matrixStack] must already include the world-to-camera transform. [depth] enables terrain occlusion.
     * Chroma color is sampled once per box so its edges share one RGBA value.
     */
    fun drawOutlinedBoundingBox(
        matrixStack: UMatrixStack,
        block: BlockPos,
        c: ChromaColour,
        width: Float,
        depth: Boolean = true,
    ) {
        if (!width.isFinite() || width <= 0) return

        val color = c.getEffectiveColour()
        if (color.alpha == 0) return

        val cameraPosition = getMinecraft().gameRenderer.mainCamera.position()
        val buffer = UBufferBuilder.create(UGraphics.DrawMode.QUADS, UGraphics.CommonVertexFormats.POSITION_COLOR)
        writeOutlinedBox(buffer, matrixStack, AABB(block), cameraPosition, color, width)
        LineRenderer.drawBuffer(buffer, depth)
    }

    /**
     * Draws the six faces of [block] as alpha-blended quads in one submission.
     * [matrixStack] must already include the world-to-camera transform; [depth] enables terrain occlusion.
     * Faces are two-sided so the box remains visible from inside. Transparent colors are skipped.
     */
    fun drawFilledBoundingBox(
        matrixStack: UMatrixStack,
        block: BlockPos,
        c: ChromaColour,
        depth: Boolean = true,
    ) {
        val color = c.getEffectiveColour()
        if (color.alpha == 0) return

        val buffer = UBufferBuilder.create(UGraphics.DrawMode.QUADS, UGraphics.CommonVertexFormats.POSITION_COLOR)
        writeFilledBox(buffer, matrixStack, AABB(block), color)
        buffer.build()?.drawAndClose(if (depth) boxPipeline else noDepthBoxPipeline)
    }

    /** Writes twelve independent edge ribbons into the same quad buffer. */
    internal fun writeOutlinedBox(
        buffer: UVertexConsumer,
        matrices: UMatrixStack,
        box: AABB,
        cameraPosition: Vec3,
        color: Color,
        width: Float,
    ) {
        if (color.alpha == 0 || !width.isFinite() || width <= 0) return

        val corners = Array(8) { corner ->
            Vec3(
                if (corner and 1 == 0) box.minX else box.maxX,
                if (corner and 2 == 0) box.minY else box.maxY,
                if (corner and 4 == 0) box.minZ else box.maxZ,
            )
        }

        for (index in edges.indices step 2) {
            LineRenderer.writeLine(
                buffer, matrices, corners[edges[index]], corners[edges[index + 1]], cameraPosition, color, width,
            )
        }
    }

    /** Writes six outward-wound faces without temporary corner vectors or degenerate strip triangles. */
    internal fun writeFilledBox(buffer: UVertexConsumer, matrices: UMatrixStack, box: AABB, color: Color) {
        if (color.alpha == 0) return

        val transform = matrices.peek().model

        for (corner in faces) {
            LineRenderer.writeVertex(
                buffer,
                transform,
                if (corner and 1 == 0) box.minX else box.maxX,
                if (corner and 2 == 0) box.minY else box.maxY,
                if (corner and 4 == 0) box.minZ else box.maxZ,
                color,
            )
        }
    }

    /** Uses explicit depth settings; UniversalCraft's default builder disables depth testing. */
    internal fun pipelineBuilder(depth: Boolean): URenderPipeline.Builder =
        URenderPipeline.builderWithDefaultShader(
            if (depth) "eurybium:pipeline/box" else "eurybium:pipeline/no_depth_box",
            UGraphics.DrawMode.QUADS,
            UGraphics.CommonVertexFormats.POSITION_COLOR,
        ).apply {
            blendState = BlendState.ALPHA
            depthTest = if (depth) URenderPipeline.DepthTest.LessOrEqual else URenderPipeline.DepthTest.Always
            // Overlay translucency must not become an occluder for later renderers.
            depthMask = false
            culling = false
        }
}
