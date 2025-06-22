package github.businessdirt.eurybium.events.minecraft

import github.businessdirt.eurybium.core.events.EurybiumEvent
import net.minecraft.client.world.ClientWorld

class WorldChangeEvent(val world: ClientWorld) : EurybiumEvent()
