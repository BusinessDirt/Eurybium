package github.businessdirt.eurybium.mixins.render

import github.businessdirt.eurybium.events.minecraft.rendering.WorldRenderEvent
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.RenderTickCounter
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(GameRenderer::class)
class MixinGameRenderer {

    @Inject(method = ["renderWorld"], at = [At(value = "constant", args=["stringValue=hand"], shift = At.Shift.BEFORE)])
    fun renderWorld(renderTickCounter: RenderTickCounter, ci: CallbackInfo) {
        //WorldRenderEvent(renderTickCounter.getTickProgress(false)).post()
    }
}