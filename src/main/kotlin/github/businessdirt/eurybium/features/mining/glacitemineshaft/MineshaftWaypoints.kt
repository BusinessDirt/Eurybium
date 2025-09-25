package github.businessdirt.eurybium.features.mining.glacitemineshaft

import gg.essential.universal.UMatrixStack
import github.businessdirt.eurybium.commands.CommandCategory
import github.businessdirt.eurybium.commands.brigadier.BrigadierArguments
import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.core.logging.Chat
import github.businessdirt.eurybium.core.rendering.BlockOutlineRenderer
import github.businessdirt.eurybium.core.rendering.LineRenderer
import github.businessdirt.eurybium.core.rendering.RenderState
import github.businessdirt.eurybium.core.scanner.GemstoneNodeScanner
import github.businessdirt.eurybium.core.storage.GemstoneNode
import github.businessdirt.eurybium.core.storage.GemstoneNodeStorage
import github.businessdirt.eurybium.events.CommandRegistrationEvent
import github.businessdirt.eurybium.events.minecraft.rendering.WorldRenderEvent
import github.businessdirt.eurybium.events.skyblock.MineshaftEnteredEvent
import github.businessdirt.eurybium.features.types.MineshaftType
import github.businessdirt.eurybium.utils.concurrent.Coroutine
import net.minecraft.util.math.Vec3d

object MineshaftWaypoints {

    val scanner = GemstoneNodeScanner()
    val storage = GemstoneNodeStorage(MineshaftType.JASP1.name)

    @HandleEvent
    fun onMineshaftEnteredEvent(event: MineshaftEnteredEvent) {

    }

    @HandleEvent
    fun onWorldRenderEvent(event: WorldRenderEvent) {
        RenderState.disableCull()

        val matrixStack = UMatrixStack.Compat.get()
        LineRenderer.draw3DLine(matrixStack, Vec3d(-124.0, 30.0, -196.0), Vec3d(-124.0, 29.0, -196.0))

        RenderState.enableCull()
    }

    @HandleEvent()
    fun onCommandRegistrationEvent(event: CommandRegistrationEvent) {
        event.register("euryibiumscanworldforgemstones") {
            category = CommandCategory.DEVELOPER_TEST
            description = "Scans the world for gemstone blocks"
            simpleCallback {
                Coroutine.launch("MineshaftWaypoints::onCommandRegistrationEvent") {
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