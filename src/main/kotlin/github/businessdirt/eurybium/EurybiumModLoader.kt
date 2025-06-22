package github.businessdirt.eurybium

import github.businessdirt.eurybium.core.events.EurybiumEventBus
import github.businessdirt.eurybium.events.utils.PostModInitializationEvent
import github.businessdirt.eurybium.events.utils.PreModInitializationEvent
import net.fabricmc.api.ModInitializer

class EurybiumModLoader : ModInitializer {
    override fun onInitialize() {
        EurybiumMod.preInit()
        EurybiumEventBus.init()

        PreModInitializationEvent().post()
        EurybiumMod.initialize()
        PostModInitializationEvent().post()
    }
}
