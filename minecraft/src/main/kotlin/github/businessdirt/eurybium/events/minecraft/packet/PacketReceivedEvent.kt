package github.businessdirt.eurybium.events.minecraft.packet

import github.businessdirt.eurybium.core.events.CancellableEurybiumEvent
import net.minecraft.network.protocol.Packet

class PacketReceivedEvent(val packet: Packet<*>) : CancellableEurybiumEvent()
