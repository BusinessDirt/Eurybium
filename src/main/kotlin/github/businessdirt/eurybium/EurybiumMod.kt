package github.businessdirt.eurybium

import github.businessdirt.eurybium.commands.CommandCategory
import github.businessdirt.eurybium.config.EurybiumConfig
import github.businessdirt.eurybium.config.GemstoneNodeData
import github.businessdirt.eurybium.config.OrderedWaypointsRoutes
import github.businessdirt.eurybium.config.manager.ConfigManager
import github.businessdirt.eurybium.core.events.EurybiumEventBus
import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.core.logging.ChatFormatter
import github.businessdirt.eurybium.core.logging.ChatLogger
import github.businessdirt.eurybium.core.modules.EurybiumModule
import `package github`.businessdirt.eurybium.core.modules.LoadedModules
import github.businessdirt.eurybium.events.CommandRegistrationEvent
import github.businessdirt.eurybium.events.SecondPassedEvent
import github.businessdirt.eurybium.utils.Reference
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.config.Configurator

@EurybiumModule
object EurybiumMod {

    val modules: MutableList<Any> = ArrayList()

    val logger: ChatLogger = ChatLogger()

    lateinit var configManager: ConfigManager
    var config: EurybiumConfig = EurybiumConfig()
    var gemstoneNodes: GemstoneNodeData = GemstoneNodeData()
    var orderedWaypointsRoutes: OrderedWaypointsRoutes = OrderedWaypointsRoutes()

    /**
     * Runs before the [github.businessdirt.eurybium.core.events.EurybiumEventBus] is initialized.
     */
    fun preInit() {
        LoadedModules.modules.forEach { EurybiumModLoader.loadModule(it) }
        EurybiumEventBus.init(modules)

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
