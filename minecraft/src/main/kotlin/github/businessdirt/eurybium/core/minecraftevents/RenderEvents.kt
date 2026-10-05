package github.businessdirt.eurybium.core.minecraftevents

import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.events.PreModInitializationEvent
import github.businessdirt.eurybium.events.minecraft.rendering.*
import github.businessdirt.eurybium.processors.EurybiumModule
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents

@EurybiumModule
object RenderEvents {
    private var registered = false

    @HandleEvent
    fun registerWorldRenderEvents(event: PreModInitializationEvent) {
        if (registered) return
        registered = true
        // This phase follows both solid and translucent entity geometry.
        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register { context ->
            WorldRenderAfterEntitiesEvent(context).post()
        }
        LevelRenderEvents.END_MAIN.register { context ->
            WorldRenderLastEvent(context).post()
            context.bufferSource().endBatch()
        }
    }
}
