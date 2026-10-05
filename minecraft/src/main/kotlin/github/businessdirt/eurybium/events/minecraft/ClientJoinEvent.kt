package github.businessdirt.eurybium.events.minecraft

import github.businessdirt.eurybium.core.events.EurybiumEvent
import net.minecraft.network.Connection

class ClientJoinEvent(val connection: Connection) : EurybiumEvent()
