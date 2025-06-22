package github.businessdirt.eurybium

import github.businessdirt.eurybium.commands.CommandCategory
import github.businessdirt.eurybium.config.EurybiumConfig
import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.core.logging.ChatFormatter
import github.businessdirt.eurybium.core.logging.ChatLogger
import github.businessdirt.eurybium.events.CommandRegistrationEvent
import github.businessdirt.eurybium.events.SecondPassedEvent
import github.businessdirt.eurybium.features.mining.glacitemineshaft.MineshaftWaypoints
import github.businessdirt.eurybium.utils.Reference
import net.minecraft.client.option.KeyBinding
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.config.Configurator
import org.lwjgl.glfw.GLFW

object EurybiumMod {

    val logger: ChatLogger = ChatLogger()
    val config: EurybiumConfig = EurybiumConfig()

    /**
     * Runs before the [github.businessdirt.eurybium.core.events.EurybiumEventBus] is initialized.
     */
    fun preInit() {
        Configurator.setLevel("com.mojang.authlib.yggdrasil", Level.FATAL)
        ChatFormatter.initialize(EurybiumMod::class.java)
        logger.initialize(EurybiumMod::class.java, config.dev.debug::enabled)
    }

    fun initialize() {
        config.initialize()
        SecondPassedEvent.schedule()
    }

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register(Reference.MOD_ID) {
            category = CommandCategory.MAIN
            aliases = mutableListOf("eyb")
            description = "Opens the main Eurybium config"
            simpleCallback { config.display() }
        }
    }
}
