package github.businessdirt.eurybium.minecraft.mixin;

import github.businessdirt.eurybium.api.minecraft.chat.ChatAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Handles Eurybium actions before vanilla's Custom handler can emit a server packet. */
@Mixin(Screen.class)
public abstract class ScreenChatClickMixin {
    @Inject(method = {"defaultHandleClickEvent", "defaultHandleGameClickEvent"}, at = @At("HEAD"), cancellable = true)
    private static void eurybiumHandleChatAction(ClickEvent event, Minecraft minecraft, Screen screen, CallbackInfo callback) {
        if (ChatAPI.handleCustomClick(event)) callback.cancel();
    }
}
