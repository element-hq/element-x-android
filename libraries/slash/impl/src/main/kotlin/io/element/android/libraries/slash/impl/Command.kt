/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.slash.impl

import androidx.annotation.StringRes

/**
 * Defines the command line operations.
 * The user can write these messages to perform some actions.
 * The list will be displayed in this order.
 */
enum class Command(
    val command: String,
    val aliases: List<String>?,
    val parameters: String?,
    @StringRes val description: Int,
    val isDevCommand: Boolean,
    val isAllowedInThread: Boolean
) {
    CRASH_APP("/crash", null, null, R.string.slash_command_description_crash_application, true, true),
    EMOTE("/me", null, "<message>", R.string.slash_command_description_emote, false, true),
    BAN_USER("/ban", null, "<user-id> [reason]", R.string.slash_command_description_ban_user, false, true),
    UNBAN_USER("/unban", null, "<user-id> [reason]", R.string.slash_command_description_unban_user, false, true),
    IGNORE_USER("/ignore", null, "<user-id> [reason]", R.string.slash_command_description_ignore_user, false, true),
    UNIGNORE_USER("/unignore", null, "<user-id>", R.string.slash_command_description_unignore_user, false, true),
    SET_USER_POWER_LEVEL("/op", null, "<user-id> [<power-level>]", R.string.slash_command_description_op_user, false, false),
    RESET_USER_POWER_LEVEL("/deop", null, "<user-id>", R.string.slash_command_description_deop_user, false, false),
    ROOM_NAME("/roomname", null, "<name>", R.string.slash_command_description_room_name, false, false),
    INVITE("/invite", null, "<user-id> [reason]", R.string.slash_command_description_invite_user, false, true),
    JOIN_ROOM("/join", listOf("/j", "/goto"), "<room-address> [reason]", R.string.slash_command_description_join_room, false, false),
    TOPIC("/topic", null, "<topic>", R.string.slash_command_description_topic, false, false),
    REMOVE_USER("/remove", listOf("/kick"), "<user-id> [reason]", R.string.slash_command_description_remove_user, false, true),
    CHANGE_DISPLAY_NAME("/nick", null, "<display-name>", R.string.slash_command_description_nick, false, true),
    CHANGE_DISPLAY_NAME_FOR_ROOM("/myroomnick", listOf("/roomnick"), "<display-name>", R.string.slash_command_description_nick_for_room, false, false),

    // Dev command since user has to know the mxc url
    ROOM_AVATAR("/roomavatar", null, "<mxc_url>", R.string.slash_command_description_room_avatar, true, false),

    // Dev command since user has to know the mxc url
    CHANGE_AVATAR_FOR_ROOM("/myroomavatar", null, "<mxc_url>", R.string.slash_command_description_avatar_for_room, true, false),
    RAINBOW("/rainbow", null, "<message>", R.string.slash_command_description_rainbow, false, true),
    RAINBOW_EMOTE("/rainbowme", null, "<message>", R.string.slash_command_description_rainbow_emote, false, true),
    DEVTOOLS("/devtools", null, null, R.string.slash_command_description_devtools, true, true),
    SPOILER("/spoiler", null, "<message>", R.string.slash_command_description_spoiler, false, true),
    SHRUG("/shrug", null, "<message>", R.string.slash_command_description_shrug, false, true),
    LENNY("/lenny", null, "<message>", R.string.slash_command_description_lenny, false, true),
    PLAIN("/plain", null, "<message>", R.string.slash_command_description_plain, false, true),
    WHOIS("/whois", null, "<user-id>", R.string.slash_command_description_whois, false, true),
    DISCARD_SESSION("/discardsession", null, null, R.string.slash_command_description_discard_session, false, false),
    CONFETTI("/confetti", null, "<message>", R.string.slash_command_confetti, false, false),
    SNOWFALL("/snowfall", null, "<message>", R.string.slash_command_snow, false, false),
    LEAVE_ROOM("/leave", listOf("/part"), null, R.string.slash_command_description_leave_room, true, false),
    UPGRADE_ROOM("/upgraderoom", null, "newVersion", R.string.slash_command_description_upgrade_room, true, false),
    TABLE_FLIP("/tableflip", null, "<message>", R.string.slash_command_description_table_flip, false, true);

    val allAliases = listOf(command) + aliases.orEmpty()
    fun matches(inputCommand: CharSequence) = allAliases.any { it.contentEquals(inputCommand, true) }
    fun startsWith(input: CharSequence) =
        allAliases.any { it.startsWith(input, 1, true) }
}
