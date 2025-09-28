package github.businessdirt.eurybium.mixins.render;

import github.businessdirt.eurybium.core.rendering.GlowingBlockRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {

    @Inject(method = "renderBlockEntities", at = @At("TAIL"))
    private void renderCustomOutline(
            MatrixStack matrixStack,
            VertexConsumerProvider.Immediate entityVertexConsumers,
            VertexConsumerProvider.Immediate effectVertexConsumers,
            Camera camera,
            float tickProgress,
            CallbackInfo ci
    ) {
        GlowingBlockRenderer.INSTANCE.render(matrixStack, camera);
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderMain(Lnet/minecraft/client/render/FrameGraphBuilder;Lnet/minecraft/client/render/Frustum;Lnet/minecraft/client/render/Camera;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/client/render/Fog;ZZLnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/util/profiler/Profiler;)V"), index = 6)
    private boolean forceEntityOutline(boolean renderEntityOutline) {
        return renderEntityOutline || GlowingBlockRenderer.INSTANCE.shouldRender();
    }

    @Inject(method = "getEntitiesToRender", at = @At("RETURN"), cancellable = true)
    private void forceEntityOutlineReturn(Camera camera, Frustum frustum, List<Entity> output, CallbackInfoReturnable<Boolean> cir) {
        if (GlowingBlockRenderer.INSTANCE.shouldRender()) {
            cir.setReturnValue(true);
        }
    }
}
