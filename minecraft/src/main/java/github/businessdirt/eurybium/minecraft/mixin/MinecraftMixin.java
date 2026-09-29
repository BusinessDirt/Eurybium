package github.businessdirt.eurybium.minecraft.mixin;

import github.businessdirt.eurybium.minecraft.MinecraftHooks;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Small working injection; keep version-specific targets in this module. */
@Mixin(Minecraft.class)
abstract class MinecraftMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void eurybium$afterClientTick(CallbackInfo ci) {
        MinecraftHooks.onClientTick();
    }
}
