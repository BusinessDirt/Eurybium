package github.businessdirt.eurybium.core.minecraftevents

import github.businessdirt.eurybium.core.events.EventCallback
import github.businessdirt.eurybium.events.minecraft.rendering.WorldRenderLastEvent
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents

object RenderEvents {

    @EventCallback
    fun registerWorldRenderEvents() {
        WorldRenderEvents.LAST.register(WorldRenderEvents.Last { context ->
            WorldRenderLastEvent(context).post()
        })
    }
}
