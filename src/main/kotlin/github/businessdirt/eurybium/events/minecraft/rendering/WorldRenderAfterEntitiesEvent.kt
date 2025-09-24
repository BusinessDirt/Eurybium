package github.businessdirt.eurybium.events.minecraft.rendering

import github.businessdirt.eurybium.core.events.RenderingEurybiumEvent
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext

class WorldRenderAfterEntitiesEvent(override val context: WorldRenderContext) : RenderingEurybiumEvent(context)