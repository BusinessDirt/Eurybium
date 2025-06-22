package github.businessdirt.eurybium.events.minecraft

import github.businessdirt.eurybium.core.events.EurybiumEvent
import net.minecraft.network.ClientConnection

class ClientJoinEvent(val connection: ClientConnection) : EurybiumEvent()
