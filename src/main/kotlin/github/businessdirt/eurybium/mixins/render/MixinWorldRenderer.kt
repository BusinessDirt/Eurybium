package github.businessdirt.eurybium.mixins.render

import github.businessdirt.eurybium.core.rendering.GlowingBlockRenderer
import net.minecraft.client.render.Camera
import net.minecraft.client.render.Frustum
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.ModifyArg
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import java.util.List

@Mixin(WorldRenderer::class)
class MixinWorldRenderer {

    var hasActiveBlockOutlines = false

    @Inject(method = ["renderBlockEntities"], at = [At("TAIL")])
    fun renderBlockEntities(
        matrixStack: MatrixStack,
        entityVertexConsumers: VertexConsumerProvider.Immediate,
        effectVertexConsumers: VertexConsumerProvider.Immediate,
        camera: Camera,
        tickProgress: Float,
        ci: CallbackInfo
    ) {
        GlowingBlockRenderer.render(matrixStack, camera)
    }

    @ModifyArg(method = ["render"], at = At(
        value = "INVOKE",
        target = "Lnet/minecraft/client/render/WorldRenderer;renderMain(Lnet/minecraft/client/render/FrameGraphBuilder;Lnet/minecraft/client/render/Frustum;Lnet/minecraft/client/render/Camera;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;ZZLnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/util/profiler/Profiler;)V"
    ), index = 6)
    fun render(renderEntityOutline: Boolean): Boolean {
        return renderEntityOutline || GlowingBlockRenderer.shouldRender()
    }

    @Inject(method = ["getEntitiesToRender"], at = [At("RETURN")], cancellable = true)
    fun getEntitiesToRender(camera: Camera, frustum: Frustum, output: List<Entity>, cir: CallbackInfoReturnable<Boolean>) {
        if (GlowingBlockRenderer.shouldRender()) {
            cir.returnValue = true
        }
    }
}