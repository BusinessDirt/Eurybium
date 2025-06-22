package github.businessdirt.eurybium.events.minecraft.packet

import github.businessdirt.eurybium.core.events.CancellableEurybiumEvent
import net.minecraft.network.packet.Packet

class PacketReceivedEvent(val packet: Packet<*>) : CancellableEurybiumEvent()
