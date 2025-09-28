package github.businessdirt.eurybium.events.minecraft

import com.mojang.authlib.GameProfile
import github.businessdirt.eurybium.core.events.CancellableEurybiumEvent
import github.businessdirt.eurybium.core.events.EurybiumEvent
import net.minecraft.network.message.MessageType
import net.minecraft.network.message.SignedMessage
import net.minecraft.text.Text
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
    val message: Text,
    val signedMessage: SignedMessage?,
    val sender: GameProfile?,
    val params: MessageType.Parameters,
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
    val message: Text,
    val signedMessage: SignedMessage?,
    val sender: GameProfile?,
    val params: MessageType.Parameters,
    val receptionTimestamp: Instant
) : EurybiumEvent()

/**
 * An event triggered when receiving a chat message is cancelled with [AllowChatMessageEvent].
 */
@Suppress("unused")
class ChatMessageCancelledEvent(
    val message: Text,
    val signedMessage: SignedMessage?,
    val sender: GameProfile?,
    val params: MessageType.Parameters,
    val receptionTimestamp: Instant
) : EurybiumEvent()