package github.businessdirt.eurybium.core.minecraftevents

import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.core.modules.EurybiumModule
import github.businessdirt.eurybium.events.minecraft.rendering.WorldRenderLastEvent
import github.businessdirt.eurybium.events.utils.PreModInitializationEvent
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents

@EurybiumModule
object RenderEvents {

    @HandleEvent
    fun registerWorldRenderEvents(event: PreModInitializationEvent) {
        WorldRenderEvents.LAST.register(WorldRenderEvents.Last { context ->
            WorldRenderLastEvent(context).post()
        })
    }
}
