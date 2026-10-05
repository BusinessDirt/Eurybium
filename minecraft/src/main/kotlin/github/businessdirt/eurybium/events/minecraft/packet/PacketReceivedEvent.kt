package github.businessdirt.eurybium.events.minecraft.packet

import github.businessdirt.eurybium.api.events.CancellableEurybiumEvent
import net.minecraft.network.protocol.Packet

class PacketReceivedEvent(val packet: Packet<*>) : CancellableEurybiumEvent()
