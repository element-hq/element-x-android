/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.slashcommands.impl

import dev.zacsweers.metro.Inject
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.core.MatrixPatterns
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.mxc.isMxcUrl
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import io.element.android.libraries.slashcommands.api.ChatEffect
import io.element.android.libraries.slashcommands.api.MessagePrefix
import io.element.android.libraries.slashcommands.api.SlashCommand
import io.element.android.services.toolbox.api.strings.StringProvider
import kotlinx.coroutines.flow.first
import timber.log.Timber

@Inject
class CommandParser(
    private val appPreferencesStore: AppPreferencesStore,
    private val featureFlagService: FeatureFlagService,
    private val stringProvider: StringProvider,
) {
    /**
     * Convert the text message into a Slash command.
     *
     * @param textMessage the text message in plain text
     * @param formattedMessage the text messaged in HTML format
     * @param isInThreadTimeline true if the user is currently typing in a thread
     * @return a parsed slash command (ok or error)
     */
    suspend fun parseSlashCommand(
        textMessage: CharSequence,
        formattedMessage: String?,
        isInThreadTimeline: Boolean,
    ): SlashCommand {
        if (!featureFlagService.isFeatureEnabled(FeatureFlags.SlashCommand)) {
            return SlashCommand.NotACommand
        }
        // check if it has the Slash marker
        val message = formattedMessage ?: textMessage
        return if (!message.startsWith("/")) {
            SlashCommand.NotACommand
        } else {
            // "/" only
            if (message.length == 1) {
                return SlashCommand.ErrorEmptySlashCommand(
                    stringProvider.getString(R.string.slash_command_unrecognized, "/")
                )
            }
            // Exclude "//"
            if ("/" == message.substring(1, 2)) {
                return SlashCommand.NotACommand
            }
            val (messageParts, message) = extractMessage(message.toString())
                ?: return SlashCommand.ErrorEmptySlashCommand(
                    stringProvider.getString(R.string.slash_command_unrecognized, "/")
                )
            val slashCommand = messageParts.first()
            getNotSupportedByThreads(isInThreadTimeline, slashCommand)?.let {
                return SlashCommand.ErrorCommandNotSupportedInThreads(
                    stringProvider.getString(
                        R.string.slash_command_not_supported_in_threads,
                        it.command,
                    )
                )
            }
            when {
                Command.PLAIN.matches(slashCommand) -> {
                    if (message.isNotEmpty()) {
                        SlashCommand.SendPlainText(message = message)
                    } else {
                        syntaxError(Command.PLAIN)
                    }
                }
                Command.CHANGE_DISPLAY_NAME.matches(slashCommand) -> {
                    if (message.isNotEmpty()) {
                        SlashCommand.ChangeDisplayName(displayName = message)
                    } else {
                        syntaxError(Command.CHANGE_DISPLAY_NAME)
                    }
                }
                Command.CHANGE_DISPLAY_NAME_FOR_ROOM.matches(slashCommand) -> {
                    if (message.isNotEmpty()) {
                        SlashCommand.ChangeDisplayNameForRoom(displayName = message)
                    } else {
                        syntaxError(Command.CHANGE_DISPLAY_NAME_FOR_ROOM)
                    }
                }
                Command.ROOM_AVATAR.matches(slashCommand) -> {
                    if (messageParts.size == 2) {
                        val url = messageParts[1]
                        if (url.isMxcUrl()) {
                            SlashCommand.ChangeRoomAvatar(url)
                        } else {
                            syntaxError(Command.ROOM_AVATAR)
                        }
                    } else {
                        syntaxError(Command.ROOM_AVATAR)
                    }
                }
                Command.CHANGE_AVATAR.matches(slashCommand) -> {
                    if (messageParts.size == 2) {
                        val url = messageParts[1]
                        if (url.isMxcUrl()) {
                            SlashCommand.ChangeAvatar(url)
                        } else {
                            syntaxError(Command.CHANGE_AVATAR)
                        }
                    } else {
                        syntaxError(Command.CHANGE_AVATAR)
                    }
                }
                Command.CHANGE_AVATAR_FOR_ROOM.matches(slashCommand) -> {
                    if (messageParts.size == 2) {
                        val url = messageParts[1]

                        if (url.isMxcUrl()) {
                            SlashCommand.ChangeAvatarForRoom(url)
                        } else {
                            syntaxError(Command.CHANGE_AVATAR_FOR_ROOM)
                        }
                    } else {
                        syntaxError(Command.CHANGE_AVATAR_FOR_ROOM)
                    }
                }
                Command.TOPIC.matches(slashCommand) -> {
                    if (message.isNotEmpty()) {
                        SlashCommand.ChangeTopic(topic = message)
                    } else {
                        syntaxError(Command.TOPIC)
                    }
                }
                Command.EMOTE.matches(slashCommand) -> {
                    if (message.isNotEmpty()) {
                        SlashCommand.SendEmote(message)
                    } else {
                        syntaxError(Command.EMOTE)
                    }
                }
                Command.RAINBOW.matches(slashCommand) -> {
                    if (message.isNotEmpty()) {
                        SlashCommand.SendRainbow(message)
                    } else {
                        syntaxError(Command.RAINBOW)
                    }
                }
                Command.RAINBOW_EMOTE.matches(slashCommand) -> {
                    if (message.isNotEmpty()) {
                        SlashCommand.SendRainbowEmote(message)
                    } else {
                        syntaxError(Command.RAINBOW_EMOTE)
                    }
                }
                Command.JOIN_ROOM.matches(slashCommand) -> {
                    if (messageParts.size >= 2) {
                        val id = messageParts[1]
                        val roomIdOrAlias = RoomIdOrAlias.from(id)
                        if (roomIdOrAlias != null) {
                            SlashCommand.JoinRoom(
                                RoomIdOrAlias.Id(RoomId(id)),
                                trimParts(textMessage, messageParts.take(2))
                            )
                        } else {
                            syntaxError(Command.JOIN_ROOM)
                        }
                    } else {
                        syntaxError(Command.JOIN_ROOM)
                    }
                }
                Command.ROOM_NAME.matches(slashCommand) -> {
                    if (message.isNotEmpty()) {
                        SlashCommand.ChangeRoomName(name = message)
                    } else {
                        syntaxError(Command.ROOM_NAME)
                    }
                }
                Command.INVITE.matches(slashCommand) -> {
                    if (messageParts.size >= 2) {
                        parseUserId(messageParts)
                            ?.let { userId ->
                                SlashCommand.Invite(
                                    userId = userId,
                                    reason = trimParts(textMessage, messageParts.take(2))
                                )
                            }
                            ?: syntaxError(Command.INVITE)
                    } else {
                        syntaxError(Command.INVITE)
                    }
                }
                Command.REMOVE_USER.matches(slashCommand) -> {
                    parseUserId(messageParts)
                        ?.let { userId ->
                            SlashCommand.RemoveUser(
                                userId = userId,
                                reason = trimParts(textMessage, messageParts.take(2))
                            )
                        }
                        ?: syntaxError(Command.REMOVE_USER)
                }
                Command.BAN_USER.matches(slashCommand) -> {
                    parseUserId(messageParts)
                        ?.let { userId ->
                            SlashCommand.BanUser(
                                userId = userId,
                                reason = trimParts(textMessage, messageParts.take(2))
                            )
                        }
                        ?: syntaxError(Command.BAN_USER)
                }
                Command.UNBAN_USER.matches(slashCommand) -> {
                    parseUserId(messageParts)
                        ?.let { userId ->
                            SlashCommand.UnbanUser(
                                userId = userId,
                                reason = trimParts(textMessage, messageParts.take(2))
                            )
                        }
                        ?: syntaxError(Command.UNBAN_USER)
                }
                Command.IGNORE_USER.matches(slashCommand) -> {
                    parseUserId(messageParts)
                        ?.let { userId ->
                            SlashCommand.IgnoreUser(
                                userId = userId,
                            )
                        }
                        ?: syntaxError(Command.IGNORE_USER)
                }
                Command.UNIGNORE_USER.matches(slashCommand) -> {
                    parseUserId(messageParts)
                        ?.let { userId ->
                            SlashCommand.UnignoreUser(
                                userId = userId,
                            )
                        }
                        ?: syntaxError(Command.UNIGNORE_USER)
                }
                Command.SET_USER_POWER_LEVEL.matches(slashCommand) -> {
                    if (messageParts.size == 3) {
                        val userId = parseUserId(messageParts)
                        if (userId != null) {
                            val powerLevelsAsString = messageParts[2]
                            try {
                                val powerLevelsAsInt = Integer.parseInt(powerLevelsAsString)
                                SlashCommand.SetUserPowerLevel(
                                    userId = userId,
                                    powerLevel = powerLevelsAsInt
                                )
                            } catch (_: Exception) {
                                syntaxError(Command.SET_USER_POWER_LEVEL)
                            }
                        } else {
                            syntaxError(Command.SET_USER_POWER_LEVEL)
                        }
                    } else {
                        syntaxError(Command.SET_USER_POWER_LEVEL)
                    }
                }
                Command.RESET_USER_POWER_LEVEL.matches(slashCommand) -> {
                    parseUserId(messageParts)
                        ?.let { userId ->
                            SlashCommand.SetUserPowerLevel(
                                userId = userId,
                                powerLevel = null
                            )
                        }
                        ?: syntaxError(Command.SET_USER_POWER_LEVEL)
                }
                Command.DEVTOOLS.matches(slashCommand) -> {
                    if (messageParts.size == 1) {
                        SlashCommand.DevTools
                    } else {
                        syntaxError(Command.DEVTOOLS)
                    }
                }
                Command.SPOILER.matches(slashCommand) -> {
                    if (message.isNotEmpty()) {
                        SlashCommand.SendSpoiler(message)
                    } else {
                        syntaxError(Command.SPOILER)
                    }
                }
                Command.SHRUG.matches(slashCommand) -> {
                    SlashCommand.SendWithPrefix(MessagePrefix.Shrug, message)
                }
                Command.LENNY.matches(slashCommand) -> {
                    SlashCommand.SendWithPrefix(MessagePrefix.Lenny, message)
                }
                Command.TABLE_FLIP.matches(slashCommand) -> {
                    SlashCommand.SendWithPrefix(MessagePrefix.TableFlip, message)
                }
                Command.UNFLIP.matches(slashCommand) -> {
                    SlashCommand.SendWithPrefix(MessagePrefix.Unflip, message)
                }
                Command.DISCARD_SESSION.matches(slashCommand) -> {
                    if (messageParts.size == 1) {
                        SlashCommand.DiscardSession
                    } else {
                        syntaxError(Command.DISCARD_SESSION)
                    }
                }
                Command.WHOIS.matches(slashCommand) -> {
                    parseUserId(messageParts)
                        ?.let { userId ->
                            SlashCommand.ShowUser(
                                userId = userId,
                            )
                        }
                        ?: syntaxError(Command.WHOIS)
                }
                Command.CONFETTI.matches(slashCommand) -> {
                    SlashCommand.SendChatEffect(ChatEffect.CONFETTI, message)
                }
                Command.SNOWFALL.matches(slashCommand) -> {
                    SlashCommand.SendChatEffect(ChatEffect.SNOWFALL, message)
                }
                Command.LEAVE_ROOM.matches(slashCommand) -> {
                    if (messageParts.size == 1) {
                        SlashCommand.LeaveRoom
                    } else {
                        syntaxError(Command.LEAVE_ROOM)
                    }
                }
                Command.UPGRADE_ROOM.matches(slashCommand) -> {
                    if (message.isNotEmpty()) {
                        SlashCommand.UpgradeRoom(newVersion = message)
                    } else {
                        syntaxError(Command.UPGRADE_ROOM)
                    }
                }
                Command.CRASH_APP.matches(slashCommand) && appPreferencesStore.isDeveloperModeEnabledFlow().first() -> {
                    error("Application crashed from user demand")
                }
                else -> {
                    // Unknown command
                    SlashCommand.ErrorUnknownSlashCommand(
                        stringProvider.getString(R.string.slash_command_unrecognized, slashCommand)
                    )
                }
            }
        }
    }

    private fun parseUserId(messageParts: List<String>): UserId? {
        val str = messageParts.getOrNull(1) ?: return null
        return when {
            MatrixPatterns.isUserId(str) -> str
            str == "<a" -> {
                // Rich text editor mode
                messageParts.getOrNull(2)?.let { html ->
                    // html must match "href="https://matrix.to/#/@user:domain.org">@user:domain.org</a>"
                    val regex = "href=\"https://matrix.to/#/([^\"]+)\">([^<]+)</a>".toRegex()
                    val matchResult = regex.find(html)
                    val userId = matchResult?.groupValues?.getOrNull(1)
                    userId?.takeIf {
                        userId == matchResult.groupValues.getOrNull(2) && MatrixPatterns.isUserId(it)
                    }
                }
            }
            else -> {
                // Can be markdown format like "[@user:domain.org](https://matrix.to/#/@user:domain.org)"
                val regex = "\\[([^\\]]+)]\\(https://matrix.to/#/([^\\]]+)\\)".toRegex()
                val matchResult = regex.find(str)
                val userId = matchResult?.groupValues?.getOrNull(1)
                userId?.takeIf {
                    userId == matchResult.groupValues.getOrNull(2) && MatrixPatterns.isUserId(it)
                }
            }
        }
            ?.let(::UserId)
    }

    private fun syntaxError(command: Command) = SlashCommand.ErrorSyntax(
        stringProvider.getString(
            R.string.slash_command_parameters_error,
            command.command,
            buildString {
                append(command.command)
                if (command.parameters != null) {
                    append(" ${command.parameters}")
                }
            },
        )
    )

    private fun extractMessage(message: String): Pair<List<String>, String>? {
        val messageParts = try {
            message.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }
        } catch (e: Exception) {
            Timber.e(e, "## parseSlashCommand() : split failed")
            null
        }

        // test if the string cut fails
        if (messageParts.isNullOrEmpty()) {
            return null
        }

        val slashCommand = messageParts.first()
        val trimmedMessage = message.substring(slashCommand.length).trim()

        return messageParts to trimmedMessage
    }

    private val notSupportedThreadsCommands: List<Command> by lazy {
        Command.entries.filter {
            !it.isAllowedInThread
        }
    }

    /**
     * Checks whether the current command is not supported by threads.
     * @param isInThreadTimeline if its true we are in a thread timeline
     * @param slashCommand the slash command that will be checked
     * @return The command that is not supported
     */
    private fun getNotSupportedByThreads(isInThreadTimeline: Boolean, slashCommand: String): Command? {
        return if (isInThreadTimeline) {
            notSupportedThreadsCommands.firstOrNull {
                it.command == slashCommand
            }
        } else {
            null
        }
    }

    private fun trimParts(message: CharSequence, messageParts: List<String>): String? {
        val partsSize = messageParts.sumOf { it.length }
        val gapsNumber = messageParts.size - 1
        return message.substring(partsSize + gapsNumber).trim().takeIf { it.isNotEmpty() }
    }
}
