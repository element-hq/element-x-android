/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.slashcommands.api

import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.UserId

/**
 * Represent a slash command.
 */
sealed interface SlashCommand {
    // This is not a Slash command
    data object NotACommand : SlashCommand

    // Slash command types:
    sealed interface Error : SlashCommand
    sealed interface SlashCommandSendMessage : SlashCommand
    sealed interface SlashCommandAdmin : SlashCommand
    sealed interface SlashCommandNavigation : SlashCommand

    // Errors
    data class ErrorEmptySlashCommand(val message: String) : Error
    data class ErrorCommandNotSupportedInThreads(val message: String) : Error

    // Unknown/Unsupported slash command
    data class ErrorUnknownSlashCommand(val message: String) : Error

    // A slash command is detected, but there is an error
    data class ErrorSyntax(val message: String) : Error

    // Valid commands:
    data class SendPlainText(val message: CharSequence) : SlashCommandSendMessage
    data class SendEmote(val message: CharSequence) : SlashCommandSendMessage
    data class SendRainbow(val message: CharSequence) : SlashCommandSendMessage
    data class SendRainbowEmote(val message: CharSequence) : SlashCommandSendMessage
    data class BanUser(val userId: UserId, val reason: String?) : SlashCommandAdmin
    data class UnbanUser(val userId: UserId, val reason: String?) : SlashCommandAdmin
    data class IgnoreUser(val userId: UserId) : SlashCommandAdmin
    data class UnignoreUser(val userId: UserId) : SlashCommandAdmin
    data class SetUserPowerLevel(val userId: UserId, val powerLevel: Int?) : SlashCommandAdmin
    data class ChangeRoomName(val name: String) : SlashCommandAdmin
    data class Invite(val userId: UserId, val reason: String?) : SlashCommandAdmin
    data class JoinRoom(val roomIdOrAlias: RoomIdOrAlias, val reason: String?) : SlashCommandAdmin
    data class ChangeTopic(val topic: String) : SlashCommandAdmin
    data class RemoveUser(val userId: UserId, val reason: String?) : SlashCommandAdmin
    data class ChangeDisplayName(val displayName: String) : SlashCommandAdmin
    data class ChangeDisplayNameForRoom(val displayName: String) : SlashCommandAdmin
    data class ChangeRoomAvatar(val url: String) : SlashCommandAdmin
    data class ChangeAvatar(val url: String) : SlashCommandAdmin
    data class ChangeAvatarForRoom(val url: String) : SlashCommandAdmin
    data class SendSpoiler(val message: String) : SlashCommandSendMessage
    data class SendWithPrefix(val prefix: MessagePrefix, val message: CharSequence) : SlashCommandSendMessage
    data object DiscardSession : SlashCommandAdmin
    data class SendChatEffect(val chatEffect: ChatEffect, val message: String) : SlashCommandSendMessage
    data object LeaveRoom : SlashCommandAdmin
    data class UpgradeRoom(val newVersion: String) : SlashCommandAdmin

    data object DevTools : SlashCommandNavigation
    data class ShowUser(val userId: UserId) : SlashCommandNavigation
}

fun SlashCommand.Error.message() = when (this) {
    is SlashCommand.ErrorEmptySlashCommand -> message
    is SlashCommand.ErrorCommandNotSupportedInThreads -> message
    is SlashCommand.ErrorUnknownSlashCommand -> message
    is SlashCommand.ErrorSyntax -> message
}
