/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.messagecomposer

sealed interface SlashCommand {
    data class Me(val message: String) : SlashCommand
    data class Topic(val topic: String) : SlashCommand
    data class Invite(val userId: String) : SlashCommand
    data class Kick(val userId: String, val reason: String?) : SlashCommand
    data class Ban(val userId: String, val reason: String?) : SlashCommand
    data class Unban(val userId: String) : SlashCommand
    data class Part(val reason: String?) : SlashCommand
    data class Plain(val message: String) : SlashCommand
    data class Shrug(val message: String?) : SlashCommand
    data class TableFlip(val message: String?) : SlashCommand
    data class UnFlip(val message: String?) : SlashCommand
    data class Lenny(val message: String?) : SlashCommand
}

data class SlashCommandInfo(
    val name: String,
    val description: String,
    val usage: String,
)

val AVAILABLE_COMMANDS = listOf(
    SlashCommandInfo("/me", "Send an emote action", "/me <action>"),
    SlashCommandInfo("/topic", "Set the room topic", "/topic <topic>"),
    SlashCommandInfo("/invite", "Invite a user", "/invite <@user:server>"),
    SlashCommandInfo("/kick", "Kick a user", "/kick <@user:server> [reason]"),
    SlashCommandInfo("/ban", "Ban a user", "/ban <@user:server> [reason]"),
    SlashCommandInfo("/unban", "Unban a user", "/unban <@user:server>"),
    SlashCommandInfo("/part", "Leave this room", "/part [reason]"),
    SlashCommandInfo("/plain", "Send without formatting", "/plain <message>"),
    SlashCommandInfo("/shrug", "Send \u00AF\\_(\u30C4)_/\u00AF", "/shrug [message]"),
    SlashCommandInfo("/tableflip", "Send (\u256F\u00B0\u25A1\u00B0)\u256F\uFE35 \u253B\u2501\u253B", "/tableflip [message]"),
    SlashCommandInfo("/unflip", "Send \u252C\u2500\u252C\u30CE( \u00BA _ \u00BA\u30CE)", "/unflip [message]"),
    SlashCommandInfo("/lenny", "Send ( \u0361\u00B0 \u035C\u0296 \u0361\u00B0)", "/lenny [message]"),
)

object SlashCommandParser {
    fun parse(text: String): SlashCommand? {
        if (!text.startsWith("/")) return null
        val parts = text.split(" ", limit = 2)
        val command = parts[0].lowercase()
        val args = parts.getOrNull(1)?.trim()?.takeIf { it.isNotEmpty() }
        return when (command) {
            "/me" -> args?.let { SlashCommand.Me(it) }
            "/topic" -> args?.let { SlashCommand.Topic(it) }
            "/invite" -> args?.let { SlashCommand.Invite(it.trim()) }
            "/kick" -> args?.let { parseUserAndReason(it) }?.let { (u, r) -> SlashCommand.Kick(u, r) }
            "/ban" -> args?.let { parseUserAndReason(it) }?.let { (u, r) -> SlashCommand.Ban(u, r) }
            "/unban" -> args?.let { SlashCommand.Unban(it.trim()) }
            "/part", "/leave" -> SlashCommand.Part(args)
            "/plain" -> args?.let { SlashCommand.Plain(it) }
            "/shrug" -> SlashCommand.Shrug(args)
            "/tableflip" -> SlashCommand.TableFlip(args)
            "/unflip" -> SlashCommand.UnFlip(args)
            "/lenny" -> SlashCommand.Lenny(args)
            else -> null
        }
    }

    private fun parseUserAndReason(text: String): Pair<String, String?>? {
        val parts = text.split(" ", limit = 2)
        val userId = parts.firstOrNull()?.trim() ?: return null
        val reason = parts.getOrNull(1)?.trim()?.takeIf { it.isNotEmpty() }
        return userId to reason
    }
}
