package github.businessdirt.eurybium.core.commands

import com.mojang.brigadier.CommandDispatcher
import github.businessdirt.eurybium.core.events.*
import github.businessdirt.eurybium.events.CommandRegistrationEvent
import github.businessdirt.eurybium.events.PostModInitializationEvent
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import kotlin.test.Test
import kotlin.test.assertEquals

class CommandRegistryTest : EventBusTestFixture() {
    class RegistrationListener {
        var calls = 0
        @HandleEvent private fun register(event: CommandRegistrationEvent) {
            calls++
            event.register("testregistry") { description = "Test"; simpleCallback {} }
        }
    }

    @Test
    fun `initialization bridges Fabric registration to the bus for each new dispatcher`() {
        withFabricCallbacksRestored {
            val listener = RegistrationListener()
            EurybiumEventBus.register(listener)
            CommandRegistry.onPostModInitializationEvent(PostModInitializationEvent)
            repeat(2) {
                val dispatcher = CommandDispatcher<FabricClientCommandSource>()
                val context = java.lang.reflect.Proxy.newProxyInstance(
                    net.minecraft.commands.CommandBuildContext::class.java.classLoader,
                    arrayOf(net.minecraft.commands.CommandBuildContext::class.java),
                ) { _, method, _ -> error("Unexpected build context call: ${method.name}") } as net.minecraft.commands.CommandBuildContext
                ClientCommandRegistrationCallback.EVENT.invoker().register(dispatcher, context)
                assertEquals(1, dispatcher.execute("testregistry", commandSource()))
            }
            assertEquals(2, listener.calls)
        }
    }

    // Fabric exposes registration but no unregister operation. Restore its existing
    // phases and invoker so this integration test cannot affect later tests.
    @Suppress("UNCHECKED_CAST")
    private fun withFabricCallbacksRestored(block: () -> Unit) {
        val event = ClientCommandRegistrationCallback.EVENT
        fun field(name: String) = event.javaClass.getDeclaredField(name).apply { isAccessible = true }
        val phases = field("phases").get(event) as MutableMap<Any, Any>
        val savedPhases = phases.toMap()
        val sorted = field("sortedPhases").get(event) as MutableList<Any>
        val savedSorted = sorted.toList()
        val savedListeners = phases.values.map { phase ->
            val listeners = phase.javaClass.getDeclaredField("listeners").apply { isAccessible = true }
            Triple(phase, listeners, listeners.get(phase))
        }
        val handlers = field("handlers")
        val savedHandlers = handlers.get(event)
        val invoker = net.fabricmc.fabric.api.event.Event::class.java.getDeclaredField("invoker").apply { isAccessible = true }
        val savedInvoker = invoker.get(event)
        try {
            block()
        } finally {
            savedListeners.forEach { (phase, listeners, value) -> listeners.set(phase, value) }
            phases.clear()
            phases.putAll(savedPhases)
            sorted.clear()
            sorted.addAll(savedSorted)
            handlers.set(event, savedHandlers)
            invoker.set(event, savedInvoker)
        }
    }
}
