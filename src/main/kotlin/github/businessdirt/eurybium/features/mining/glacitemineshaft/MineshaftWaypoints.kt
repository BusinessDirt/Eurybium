package github.businessdirt.eurybium.features.mining.glacitemineshaft

import github.businessdirt.eurybium.commands.CommandCategory
import github.businessdirt.eurybium.commands.brigadier.BrigadierArguments
import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.core.logging.Chat
import github.businessdirt.eurybium.core.rendering.BlockOutlineRenderer
import github.businessdirt.eurybium.core.scanner.GemstoneNodeScanner
import github.businessdirt.eurybium.core.storage.GemstoneNode
import github.businessdirt.eurybium.core.storage.GemstoneNodeStorage
import github.businessdirt.eurybium.events.CommandRegistrationEvent
import github.businessdirt.eurybium.events.skyblock.MineshaftEnteredEvent
import github.businessdirt.eurybium.features.types.MineshaftType
import github.businessdirt.eurybium.utils.concurrent.Coroutine

object MineshaftWaypoints {

    val scanner = GemstoneNodeScanner()
    val storage = GemstoneNodeStorage(MineshaftType.JASP1.name)

    fun onMineshaftEnteredEvent(event: MineshaftEnteredEvent) {

    }

    @HandleEvent()
    fun onCommandRegistrationEvent(event: CommandRegistrationEvent) {
        event.register("euryibiumscanworldforgemstones") {
            category = CommandCategory.DEVELOPER_TEST
            description = "Scans the world for gemstone blocks"
            simpleCallback {
                Coroutine.launchCoroutine {
                    scanner.find()
                    storage.setAll(scanner.clusterBlocks(false))
                    storage.save()
                }
            }
        }

        event.register("euryibiumrenderoutline") {
            category = CommandCategory.DEVELOPER_TEST
            description = "Renders a GemstoneNode"
            simpleCallback { Chat.chat("${storage.size} gemstone nodes were found!") }
            argCallback("node", BrigadierArguments.int()) { node ->
                val cluster: GemstoneNode = storage.get(node)

                //Chat.chat("Cluster $node contains ${cluster.values.sumOf { it.size }} blocks!")
                BlockOutlineRenderer.clearBlockOutlines()
                BlockOutlineRenderer.renderBlockOutlines(cluster)
            }
        }
    }
}