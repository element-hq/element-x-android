/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.slashcommands.impl

import dev.zacsweers.metro.Inject
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.timeline.MsgType
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.slashcommands.api.MessagePrefix
import io.element.android.libraries.slashcommands.api.SlashCommand
import io.element.android.libraries.slashcommands.impl.rainbow.RainbowGenerator
import io.element.android.services.toolbox.api.strings.StringProvider

@Inject
class CommandExecutor(
    private val matrixClient: MatrixClient,
    private val joinedRoom: JoinedRoom,
    private val rainbowGenerator: RainbowGenerator,
    private val stringProvider: StringProvider,
) {
    suspend fun proceedSendMessage(
        slashCommand: SlashCommand.SlashCommandSendMessage,
        timeline: Timeline,
    ): Result<Unit> {
        return when (slashCommand) {
            is SlashCommand.SendChatEffect -> sendChatEffect()
            is SlashCommand.SendEmote -> sendEmote(slashCommand, timeline)
            is SlashCommand.SendWithPrefix -> sendPrefixedMessage(slashCommand.prefix, slashCommand.message, timeline)
            is SlashCommand.SendPlainText -> sendPlainText(slashCommand, timeline)
            is SlashCommand.SendRainbow -> sendRainbow(slashCommand, timeline)
            is SlashCommand.SendRainbowEmote -> sendRainbowEmote(slashCommand, timeline)
            is SlashCommand.SendSpoiler -> sendSpoiler(slashCommand, timeline)
        }
    }

    suspend fun proceedAdmin(
        slashCommand: SlashCommand.SlashCommandAdmin,
    ): Result<Unit> {
        return when (slashCommand) {
            is SlashCommand.BanUser -> banUser(slashCommand)
            is SlashCommand.ChangeAvatar -> changeAvatar()
            is SlashCommand.ChangeAvatarForRoom -> changeAvatarForRoom()
            is SlashCommand.ChangeDisplayName -> changeDisplayName(slashCommand)
            is SlashCommand.ChangeDisplayNameForRoom -> changeDisplayNameForRoom()
            is SlashCommand.ChangeRoomAvatar -> changeRoomAvatar()
            is SlashCommand.ChangeRoomName -> changeRoomName(slashCommand)
            is SlashCommand.ChangeTopic -> changeTopic(slashCommand)
            is SlashCommand.DiscardSession -> discardSession()
            is SlashCommand.IgnoreUser -> ignoreUser(slashCommand)
            is SlashCommand.Invite -> invite(slashCommand)
            is SlashCommand.JoinRoom -> joinRoom(slashCommand)
            is SlashCommand.LeaveRoom -> leaveRoom(joinedRoom)
            is SlashCommand.RemoveUser -> removeUser(slashCommand)
            is SlashCommand.SetUserPowerLevel -> setUserPowerLevel()
            is SlashCommand.UnbanUser -> unbanUser(slashCommand)
            is SlashCommand.UnignoreUser -> unignoreUser(slashCommand)
            is SlashCommand.UpgradeRoom -> upgradeRoom()
        }
    }

    private fun upgradeRoom(): Result<Unit> {
        return Result.failure(Exception("Not yet implemented"))
    }

    private suspend fun unignoreUser(slashCommand: SlashCommand.UnignoreUser): Result<Unit> {
        return matrixClient.unignoreUser(slashCommand.userId)
    }

    private suspend fun unbanUser(slashCommand: SlashCommand.UnbanUser): Result<Unit> {
        return joinedRoom.unbanUser(slashCommand.userId, slashCommand.reason)
    }

    private fun setUserPowerLevel(): Result<Unit> {
        return Result.failure(Exception("Not yet implemented"))
    }

    private suspend fun sendSpoiler(slashCommand: SlashCommand.SendSpoiler, timeline: Timeline): Result<Unit> {
        val text = "[${stringProvider.getString(R.string.common_spoiler)}](${slashCommand.message})"
        val formattedText = "<span data-mx-spoiler>${slashCommand.message}</span>"
        return timeline.sendMessage(
            body = text,
            htmlBody = formattedText,
            intentionalMentions = emptyList(),
        )
    }

    private suspend fun sendRainbowEmote(slashCommand: SlashCommand.SendRainbowEmote, timeline: Timeline): Result<Unit> {
        val message = slashCommand.message.toString()
        return timeline.sendMessage(
            body = message,
            htmlBody = rainbowGenerator.generate(message),
            msgType = MsgType.MSG_TYPE_EMOTE,
            intentionalMentions = emptyList(),
        )
    }

    private suspend fun sendRainbow(slashCommand: SlashCommand.SendRainbow, timeline: Timeline): Result<Unit> {
        val message = slashCommand.message.toString()
        return timeline.sendMessage(
            body = message,
            htmlBody = rainbowGenerator.generate(message),
            intentionalMentions = emptyList(),
        )
    }

    private suspend fun sendPlainText(slashCommand: SlashCommand.SendPlainText, timeline: Timeline): Result<Unit> {
        return timeline.sendMessage(
            body = slashCommand.message.toString(),
            htmlBody = null,
            intentionalMentions = emptyList(),
            asPlainText = true,
        )
    }

    private suspend fun sendEmote(slashCommand: SlashCommand.SendEmote, timeline: Timeline): Result<Unit> {
        val message = slashCommand.message.toString()
        return timeline.sendMessage(
            body = message,
            htmlBody = null,
            msgType = MsgType.MSG_TYPE_EMOTE,
            intentionalMentions = emptyList(),
        )
    }

    private fun sendChatEffect(): Result<Unit> {
        return Result.failure(Exception("Not yet implemented"))
    }

    private suspend fun removeUser(slashCommand: SlashCommand.RemoveUser): Result<Unit> {
        return joinedRoom.kickUser(slashCommand.userId, slashCommand.reason)
    }

    private suspend fun leaveRoom(
        room: JoinedRoom,
    ): Result<Unit> {
        return room.leave()
    }

    private suspend fun joinRoom(slashCommand: SlashCommand.JoinRoom): Result<Unit> {
        return matrixClient.joinRoomByIdOrAlias(slashCommand.roomIdOrAlias, emptyList())
            .map {}
    }

    private suspend fun invite(slashCommand: SlashCommand.Invite): Result<Unit> {
        return joinedRoom.inviteUserById(slashCommand.userId)
    }

    private suspend fun ignoreUser(slashCommand: SlashCommand.IgnoreUser): Result<Unit> {
        return matrixClient.ignoreUser(slashCommand.userId)
    }

    private fun discardSession(): Result<Unit> {
        return Result.failure(Exception("Not yet implemented"))
    }

    private suspend fun changeTopic(slashCommand: SlashCommand.ChangeTopic): Result<Unit> {
        return joinedRoom.setTopic(slashCommand.topic)
    }

    private suspend fun changeRoomName(slashCommand: SlashCommand.ChangeRoomName): Result<Unit> {
        return joinedRoom.setName(slashCommand.name)
    }

    private fun changeRoomAvatar(): Result<Unit> {
        return Result.failure(Exception("Not yet implemented"))
    }

    private fun changeDisplayNameForRoom(): Result<Unit> {
        return Result.failure(Exception("Not yet implemented"))
    }

    private suspend fun changeDisplayName(slashCommand: SlashCommand.ChangeDisplayName): Result<Unit> {
        return matrixClient.setDisplayName(slashCommand.displayName)
    }

    private fun changeAvatar(): Result<Unit> {
        return Result.failure(Exception("Not yet implemented"))
    }

    private fun changeAvatarForRoom(): Result<Unit> {
        return Result.failure(Exception("Not yet implemented"))
    }

    private suspend fun banUser(slashCommand: SlashCommand.BanUser): Result<Unit> {
        return joinedRoom.banUser(slashCommand.userId, slashCommand.reason)
    }

    private suspend fun sendPrefixedMessage(
        prefix: MessagePrefix,
        message: CharSequence,
        timeline: Timeline,
    ): Result<Unit> {
        val sequence = buildString {
            append(prefix.toMarkdown())
            if (message.isNotEmpty()) {
                append(" ")
                append(message)
            }
        }
        return timeline.sendMessage(
            body = sequence,
            htmlBody = null,
            intentionalMentions = emptyList(),
        )
    }
}

private fun MessagePrefix.toMarkdown() = when (this) {
    MessagePrefix.Shrug -> "¯\\\\_(ツ)\\_/¯"
    MessagePrefix.TableFlip -> "(╯°□°）╯︵ ┻━┻"
    MessagePrefix.Unflip -> "┬──┬ ノ( ゜-゜ノ)"
    MessagePrefix.Lenny -> "( ͡° ͜ʖ ͡°)"
}
