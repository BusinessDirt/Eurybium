package github.businessdirt.eurybium.minecraft.mixin;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

/** Access limited to local message replacement and rebuilding the wrapped display lines. */
@Mixin(ChatComponent.class)
public interface ChatComponentAccessor {
    @Accessor("allMessages")
    List<GuiMessage> getEurybiumMessages();

    @Invoker("refreshTrimmedMessages")
    void eurybiumRefreshMessages();
}
