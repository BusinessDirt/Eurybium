package github.businessdirt.eurybium.events.minecraft.rendering

import github.businessdirt.eurybium.core.events.EurybiumEvent
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext

class WorldRenderAfterEntitiesEvent(val context: LevelRenderContext) : EurybiumEvent()