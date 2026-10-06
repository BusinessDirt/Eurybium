package github.businessdirt.eurybium.api.minecraft.events

import github.businessdirt.eurybium.api.events.HandleEvent
import github.businessdirt.eurybium.events.PreModInitializationEvent
import github.businessdirt.eurybium.events.minecraft.*
import github.businessdirt.eurybium.processors.EurybiumModule
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents

@EurybiumModule
object ClientEvents {

    var totalTicks: Long = 0
        private set

    @HandleEvent(eventType = PreModInitializationEvent::class)
    private fun onPreModInitializationEvent()  {

        ClientPlayConnectionEvents.JOIN.register { handler, _, _ -> ClientJoinEvent(handler.connection).post() }
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> ClientDisconnectEvent().post() }

        ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register { _, level -> WorldChangeEvent(level).post() }

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (client.player != null && client.level != null) {
                totalTicks++
                TickEvent(totalTicks).post()
            }
        }

        // === CHAT ===

        ClientReceiveMessageEvents.ALLOW_CHAT.register { message, signed, sender, params, timestamp ->
            !AllowChatMessageEvent(message, signed, sender, params, timestamp).post()
        }

        ClientReceiveMessageEvents.CHAT.register { message, signed, sender, params, timestamp ->
            ChatMessageReceivedEvent(message, signed, sender, params, timestamp).post()
        }

        ClientReceiveMessageEvents.CHAT_CANCELED.register { message, signed, sender, params, timestamp ->
            ChatMessageCancelledEvent(message, signed, sender, params, timestamp).post()
        }

        // === GAME ===

        ClientReceiveMessageEvents.ALLOW_GAME.register { message, overlay ->
            !AllowGameMessageEvent(message, overlay).post()
        }

        ClientReceiveMessageEvents.GAME.register { message, overlay ->
            GameMessageReceivedEvent(message, overlay).post()
        }

        ClientReceiveMessageEvents.MODIFY_GAME.register { message, overlay ->
            ModifyGameMessageEvent(message, overlay).also { it.post() }.message
        }

        ClientReceiveMessageEvents.GAME_CANCELED.register { message, overlay ->
            GameMessageCancelledEvent(message, overlay).post()
        }
    }
}
