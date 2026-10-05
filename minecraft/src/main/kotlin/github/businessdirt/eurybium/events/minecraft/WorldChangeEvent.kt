package github.businessdirt.eurybium.events.minecraft

import github.businessdirt.eurybium.core.events.EurybiumEvent
import net.minecraft.client.multiplayer.ClientLevel

class WorldChangeEvent(val world: ClientLevel) : EurybiumEvent()
