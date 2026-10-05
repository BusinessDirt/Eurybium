package github.businessdirt.eurybium.minecraft.hooks

import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.events.PreModInitializationEvent
import github.businessdirt.eurybium.events.minecraft.rendering.*
import github.businessdirt.eurybium.processors.EurybiumModule
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents

@EurybiumModule
object RenderEvents {

    @HandleEvent(eventType = PreModInitializationEvent::class)
    private fun onPreModInitializationEvent() {

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
