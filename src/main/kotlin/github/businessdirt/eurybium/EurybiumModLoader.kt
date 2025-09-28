package github.businessdirt.eurybium

import github.businessdirt.eurybium.EurybiumMod.modules
import github.businessdirt.eurybium.events.utils.PostModInitializationEvent
import github.businessdirt.eurybium.events.utils.PreModInitializationEvent
import net.fabricmc.api.ModInitializer

class EurybiumModLoader : ModInitializer {
    override fun onInitialize() {
        EurybiumMod.preInit()

        PreModInitializationEvent().post()
        EurybiumMod.initialize()
        PostModInitializationEvent().post()
    }

    companion object {
        private val loadedClasses = mutableSetOf<String>()

        fun loadModule(obj: Any) {
            if (!loadedClasses.add(obj.javaClass.name)) IllegalStateException("Module ${obj.javaClass.name} is already loaded")
            modules.add(obj)
        }
    }
}
