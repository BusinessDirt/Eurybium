package github.businessdirt.eurybium

import gg.essential.universal.UMinecraft.getMinecraft
import github.businessdirt.eurybium.commands.CommandCategory
import github.businessdirt.eurybium.config.EurybiumConfig
import github.businessdirt.eurybium.config.manager.ConfigManager
import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.core.logging.ChatFormatter
import github.businessdirt.eurybium.core.logging.ChatLogger
import github.businessdirt.eurybium.events.CommandRegistrationEvent
import github.businessdirt.eurybium.events.SecondPassedEvent
import github.businessdirt.eurybium.utils.Reference
import io.github.notenoughupdates.moulconfig.managed.ManagedConfig
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.config.Configurator
import java.io.File

object EurybiumMod {

    val logger: ChatLogger = ChatLogger()

    lateinit var configManager: ConfigManager
    var config: EurybiumConfig = EurybiumConfig()

    /**
     * Runs before the [github.businessdirt.eurybium.core.events.EurybiumEventBus] is initialized.
     */
    fun preInit() {
        Configurator.setLevel("com.mojang.authlib.yggdrasil", Level.FATAL)
        ChatFormatter.initialize(EurybiumMod::class.java)
        logger.initialize(EurybiumMod::class.java, config.dev.debug::enabled)
    }

    fun initialize() {
        configManager = ConfigManager()
        configManager.initialize()

        SecondPassedEvent.schedule()
    }

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register(Reference.MOD_ID) {
            category = CommandCategory.MAIN
            aliases = mutableListOf("eyb")
            description = "Opens the main Eurybium config"
            simpleCallback { configManager.openConfigGui() }
        }
    }
}
