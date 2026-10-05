package github.businessdirt.eurybium.minecraft.mixin;

import github.businessdirt.eurybium.events.minecraft.packet.PacketReceivedEvent;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Connection.class, priority = 1001)
abstract class ConnectionMixin {
    @Shadow public abstract PacketFlow getReceiving();

    @Inject(
        method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void eurybium$receivePacket(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        // Exclude the integrated server's server bound connections.
        if (getReceiving() == PacketFlow.CLIENTBOUND && packet != null && new PacketReceivedEvent(packet).post()) {
            ci.cancel();
        }
    }
}
