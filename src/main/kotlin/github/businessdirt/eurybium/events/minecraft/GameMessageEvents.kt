package github.businessdirt.eurybium.events.minecraft

import github.businessdirt.eurybium.core.events.CancellableEurybiumEvent
import github.businessdirt.eurybium.core.events.EurybiumEvent
import net.minecraft.text.Text

/**
 * An event triggered when the client receives a game message,
 * which is any message sent by the server.
 * Mods can use this to block the message or toggle overlay.
 *
 *
 * If the event is cancelled, the message will not be displayed,
 * the remaining listeners will be called (if any), and
 * [GameMessageCancelledEvent] will be triggered instead of [ModifyGameMessageEvent].
 *
 *
 * Overlay is whether the message will be displayed in the action bar.
 * To toggle overlay, return cancel the event and call
 * [ClientPlayerEntity.sendMessage(message, overlay)][net.minecraft.client.network.ClientPlayerEntity.sendMessage].
 */
class AllowGameMessageEvent(val message: Text, val overlay: Boolean) : CancellableEurybiumEvent()

/**
 * An event triggered when the client receives a game message,
 * which is any message sent by the server. Is not called when
 * [AllowGameMessageEvent] has been cancelled.
 * Mods can use this to listen to the message.
 *
 *
 * Overlay is whether the message will be displayed in the action bar.
 * Use [AllowGameMessageEvent] to toggle overlay.
 */
class GameMessageReceivedEvent(val message: Text, val overlay: Boolean) : EurybiumEvent()

/**
 * An event triggered when the client receives a game message,
 * which is any message sent by the server. Is not called when
 * [AllowGameMessageEvent] has been cancelled.
 * Mods can use this to modify the message.
 * Use [GameMessageReceivedEvent] if not modifying the message.
 *
 *
 * Overlay is whether the message will be displayed in the action bar.
 * Use [AllowGameMessageEvent] to toggle overlay.
 */
class ModifyGameMessageEvent(val message: Text, val overlay: Boolean) : EurybiumEvent()

/**
 * An event triggered when receiving a game message is cancelled with [AllowGameMessageEvent].
 *
 *
 * Overlay is whether the message would have been displayed in the action bar.
 */
class GameMessageCancelledEvent(val message: Text, val overlay: Boolean) : EurybiumEvent()