package github.businessdirt.eurybium.mixins.transformers

import github.businessdirt.eurybium.events.minecraft.packet.PacketReceivedEvent
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import net.minecraft.network.ClientConnection
import net.minecraft.network.packet.Packet
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(value = [ClientConnection::class], priority = 1001)
abstract class MixinClientConnection : SimpleChannelInboundHandler<Packet<*>>() {

    @Inject(method = ["channelRead0"], at = [At("HEAD")], cancellable = true)
    private fun onReceivePacket(context: ChannelHandlerContext, packet: Packet<*>?, ci: CallbackInfo) {
        if (packet != null && PacketReceivedEvent(packet).post()) {
            ci.cancel()
        }
    }
}
