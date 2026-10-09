package github.businessdirt.eurybium.minecraft.mixin;

import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

/** Reuse Minecraft's listed-player filtering, ordering, and display limit. */
@Mixin(PlayerTabOverlay.class)
public interface PlayerTabOverlayAccessor {
    @Invoker("getPlayerInfos")
    List<PlayerInfo> eurybiumPlayerInfos();

    @Accessor("header")
    Component getEurybiumHeader();

    @Accessor("footer")
    Component getEurybiumFooter();
}
