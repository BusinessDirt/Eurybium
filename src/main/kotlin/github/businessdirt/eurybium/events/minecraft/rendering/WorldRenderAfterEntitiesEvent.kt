package github.businessdirt.eurybium.events.minecraft.rendering

import github.businessdirt.eurybium.core.events.EurybiumEvent
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext

class WorldRenderAfterEntitiesEvent(val context: WorldRenderContext) : EurybiumEvent()