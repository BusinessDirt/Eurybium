package github.businessdirt.eurybium.core.minecraftevents

import github.businessdirt.eurybium.core.events.EventCallback
import github.businessdirt.eurybium.events.minecraft.rendering.WorldRenderEvent
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents

object RenderEvents {

    @EventCallback
    fun registerWorldRenderEvents() {
        WorldRenderEvents.LAST.register(WorldRenderEvents.Last { context ->
            WorldRenderEvent(context).post()
        })
    }
}
