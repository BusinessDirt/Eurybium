package github.businessdirt.eurybium.events.minecraft

import com.mojang.authlib.GameProfile
import github.businessdirt.eurybium.api.events.CancellableEurybiumEvent
import github.businessdirt.eurybium.api.events.EurybiumEvent
import net.minecraft.network.chat.ChatType
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.PlayerChatMessage
import java.time.Instant

/**
 * An event triggered when the client receives a chat message,
 * which is any message sent by a player. Mods can use this to block the message.
 *
 * If the event is cancelled, the message will not be displayed,
 * the remaining listeners will be called (if any), and
 * [ChatMessageCancelledEvent] will be triggered instead of [ChatMessageReceivedEvent].
 */
@Suppress("unused")
class AllowChatMessageEvent(
    val message: Component,
    val signedMessage: PlayerChatMessage?,
    val sender: GameProfile?,
    val params: ChatType.Bound,
    val receptionTimestamp: Instant
) : CancellableEurybiumEvent()

/**
 * An event triggered when the client receives a chat message,
 * which is any message sent by a player. Is not called when
 * [AllowChatMessageEvent] has been cancelled.
 * Mods can use this to listen to the message.
 *
 * If mods want to modify the message, they should use [AllowChatMessageEvent]
 * and manually add the new message to the chat hud using [ChatHud.addMessage(message)][net.minecraft.client.gui.hud.ChatHud.addMessage]
 */
@Suppress("unused")
class ChatMessageReceivedEvent(
    val message: Component,
    val signedMessage: PlayerChatMessage?,
    val sender: GameProfile?,
    val params: ChatType.Bound,
    val receptionTimestamp: Instant
) : EurybiumEvent()

/**
 * An event triggered when receiving a chat message is cancelled with [AllowChatMessageEvent].
 */
@Suppress("unused")
class ChatMessageCancelledEvent(
    val message: Component,
    val signedMessage: PlayerChatMessage?,
    val sender: GameProfile?,
    val params: ChatType.Bound,
    val receptionTimestamp: Instant
) : EurybiumEvent()
