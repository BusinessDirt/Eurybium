package github.businessdirt.eurybium.core.commands

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import java.lang.reflect.Proxy

internal fun commandSource(): FabricClientCommandSource = Proxy.newProxyInstance(
    FabricClientCommandSource::class.java.classLoader, arrayOf(FabricClientCommandSource::class.java),
) { _, method, _ -> error("Unexpected source call: ${method.name}") } as FabricClientCommandSource
