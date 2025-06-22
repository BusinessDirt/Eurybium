package github.businessdirt.eurybium.core.minecraftevents

import com.mojang.authlib.GameProfile
import github.businessdirt.eurybium.core.events.EventCallback
import github.businessdirt.eurybium.events.minecraft.*
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.client.world.ClientWorld
import net.minecraft.network.message.MessageType
import net.minecraft.network.message.SignedMessage
import net.minecraft.text.Text
import java.time.Instant

object ClientEvents {

    var totalTicks: Long = 0
        private set

    @EventCallback
    fun registerPlayConnectionEvents() {
        ClientPlayConnectionEvents.JOIN.register(ClientPlayConnectionEvents.Join { handler: ClientPlayNetworkHandler, sender: PacketSender, client: MinecraftClient ->
            ClientJoinEvent(handler.getConnection()).post()
        })

        ClientPlayConnectionEvents.DISCONNECT.register(ClientPlayConnectionEvents.Disconnect { handler: ClientPlayNetworkHandler, client: MinecraftClient ->
            ClientDisconnectEvent().post()
        })
    }

    @EventCallback
    fun registerWorldEvents() {
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register(ClientWorldEvents.AfterClientWorldChange { client: MinecraftClient, world: ClientWorld ->
            WorldChangeEvent(world).post()
        })
    }

    @EventCallback
    fun registerTickEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client: MinecraftClient ->
            if (MinecraftClient.getInstance().player == null) return@EndTick
            if (MinecraftClient.getInstance().world == null) return@EndTick

            totalTicks++
            TickEvent(totalTicks).post()
        })
    }

    @EventCallback
    fun registerMessageEvents() {
        ClientReceiveMessageEvents.ALLOW_CHAT.register(ClientReceiveMessageEvents.AllowChat { message: Text, signedMessage: SignedMessage?, sender: GameProfile?, params: MessageType.Parameters, receptionTimestamp: Instant ->
            !AllowChatMessageEvent(message, signedMessage, sender, params, receptionTimestamp).post()
        })

        ClientReceiveMessageEvents.CHAT.register(ClientReceiveMessageEvents.Chat { message: Text, signedMessage: SignedMessage?, sender: GameProfile?, params: MessageType.Parameters, receptionTimestamp: Instant ->
            ChatMessageReceivedEvent(message, signedMessage, sender, params, receptionTimestamp).post()
        })

        ClientReceiveMessageEvents.CHAT_CANCELED.register(ClientReceiveMessageEvents.ChatCanceled { message: Text, signedMessage: SignedMessage?, sender: GameProfile?, params: MessageType.Parameters, receptionTimestamp: Instant ->
            ChatMessageCancelledEvent(message, signedMessage, sender, params, receptionTimestamp).post()
        })

        ClientReceiveMessageEvents.ALLOW_GAME.register(ClientReceiveMessageEvents.AllowGame { message: Text, overlay: Boolean ->
            !AllowGameMessageEvent(message, overlay).post()
        })

        ClientReceiveMessageEvents.GAME.register(ClientReceiveMessageEvents.Game { message: Text, overlay: Boolean ->
            GameMessageReceivedEvent(message, overlay).post()
        })

        ClientReceiveMessageEvents.MODIFY_GAME.register((ClientReceiveMessageEvents.ModifyGame { message: Text, overlay: Boolean ->
            val event = ModifyGameMessageEvent(message, overlay)
            event.post()
            event.message
        }))

        ClientReceiveMessageEvents.GAME_CANCELED.register((ClientReceiveMessageEvents.GameCanceled { message: Text, overlay: Boolean ->
            GameMessageCancelledEvent(message, overlay).post()
        }))
    }
}
