package github.businessdirt.eurybium.mixins.network;

import github.businessdirt.eurybium.events.minecraft.packet.PacketReceivedEvent;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientConnection.class, priority = 1001)
public class MixinClientConnection {

    @Inject(method = "channelRead0*", at = @At("HEAD"), cancellable = true)
    private void onReceivePacket(ChannelHandlerContext context, @Nullable Packet<PacketListener> packet, CallbackInfo ci) {
        if (packet != null && new PacketReceivedEvent(packet).post()) {
            ci.cancel();
        }
    }
}
