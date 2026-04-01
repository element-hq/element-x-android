/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.slashcommands.impl

import androidx.annotation.StringRes

/**
 * Defines the command line operations.
 * The user can write these messages to perform some actions.
 * The list will be displayed in this order.
 */
enum class Command(
    val command: String,
    val aliases: List<String>? = null,
    val parameters: String? = null,
    @StringRes val description: Int,
    val isAllowedInThread: Boolean = true,
    val isDevCommand: Boolean = false,
) {
    CRASH_APP(
        command = "/crash",
        description = R.string.slash_command_description_crash_application,
        isDevCommand = true,
    ),
    EMOTE(
        command = "/me",
        parameters = "<message>",
        description = R.string.slash_command_description_emote,
    ),
    BAN_USER(
        command = "/ban",
        parameters = "<user-id> [reason]",
        description = R.string.slash_command_description_ban_user,
    ),
    UNBAN_USER(
        command = "/unban",
        parameters = "<user-id> [reason]",
        description = R.string.slash_command_description_unban_user,
    ),
    IGNORE_USER(
        command = "/ignore",
        parameters = "<user-id> [reason]",
        description = R.string.slash_command_description_ignore_user,
    ),
    UNIGNORE_USER(
        command = "/unignore",
        parameters = "<user-id>",
        description = R.string.slash_command_description_unignore_user,
    ),
    SET_USER_POWER_LEVEL(
        command = "/op",
        parameters = "<user-id> [<power-level>]",
        description = R.string.slash_command_description_op_user,
        isAllowedInThread = false,
    ),
    RESET_USER_POWER_LEVEL(
        command = "/deop",
        parameters = "<user-id>",
        description = R.string.slash_command_description_deop_user,
        isAllowedInThread = false,
    ),
    ROOM_NAME(
        command = "/roomname",
        parameters = "<name>",
        description = R.string.slash_command_description_room_name,
        isAllowedInThread = false,
    ),
    INVITE(
        command = "/invite",
        parameters = "<user-id> [reason]",
        description = R.string.slash_command_description_invite_user,
    ),
    JOIN_ROOM(
        command = "/join",
        aliases = listOf("/j", "/goto"),
        parameters = "<room-address> [reason]",
        description = R.string.slash_command_description_join_room,
        isAllowedInThread = false,
    ),
    TOPIC(
        command = "/topic",
        parameters = "<topic>",
        description = R.string.slash_command_description_topic,
        isAllowedInThread = false,
    ),
    REMOVE_USER(
        command = "/remove",
        aliases = listOf("/kick"),
        parameters = "<user-id> [reason]",
        description = R.string.slash_command_description_remove_user,
    ),
    CHANGE_DISPLAY_NAME(
        command = "/nick",
        parameters = "<display-name>",
        description = R.string.slash_command_description_nick,
    ),
    CHANGE_DISPLAY_NAME_FOR_ROOM(
        command = "/myroomnick",
        aliases = listOf("/roomnick"),
        parameters = "<display-name>",
        description = R.string.slash_command_description_nick_for_room,
        isAllowedInThread = false,
    ),
    ROOM_AVATAR(
        command = "/roomavatar",
        parameters = "<mxc_url>",
        description = R.string.slash_command_description_room_avatar,
        isAllowedInThread = false,
        // Dev command since user has to know the mxc url
        isDevCommand = true,
    ),
    CHANGE_AVATAR_FOR_ROOM(
        command = "/myroomavatar",
        parameters = "<mxc_url>",
        description = R.string.slash_command_description_avatar_for_room,
        isAllowedInThread = false,
        // Dev command since user has to know the mxc url
        isDevCommand = true,
    ),
    RAINBOW(
        command = "/rainbow",
        parameters = "<message>",
        description = R.string.slash_command_description_rainbow,
    ),
    RAINBOW_EMOTE(
        command = "/rainbowme",

        parameters = "<message>",
        description = R.string.slash_command_description_rainbow_emote,
    ),
    DEVTOOLS(
        command = "/devtools",
        description = R.string.slash_command_description_devtools,
        isDevCommand = true,
    ),
    SPOILER(
        command = "/spoiler",
        parameters = "<message>",
        description = R.string.slash_command_description_spoiler,
    ),
    SHRUG(
        command = "/shrug",
        parameters = "<message>",
        description = R.string.slash_command_description_shrug,
    ),
    LENNY(
        command = "/lenny",
        parameters = "<message>",
        description = R.string.slash_command_description_lenny,
    ),
    PLAIN(
        command = "/plain",
        parameters = "<message>",
        description = R.string.slash_command_description_plain,
    ),
    WHOIS(
        command = "/whois",
        parameters = "<user-id>",
        description = R.string.slash_command_description_whois,
    ),
    DISCARD_SESSION(
        command = "/discardsession",
        description = R.string.slash_command_description_discard_session,
        isAllowedInThread = false,
    ),
    CONFETTI(
        command = "/confetti",
        parameters = "<message>",
        description = R.string.slash_command_confetti,
        isAllowedInThread = false,
    ),
    SNOWFALL(
        command = "/snowfall",
        parameters = "<message>",
        description = R.string.slash_command_snow,
        isAllowedInThread = false,
    ),
    LEAVE_ROOM(
        command = "/leave",
        aliases = listOf("/part"),
        description = R.string.slash_command_description_leave_room,
        isAllowedInThread = false,
        isDevCommand = true,
    ),
    UPGRADE_ROOM(
        command = "/upgraderoom",
        parameters = "newVersion",
        description = R.string.slash_command_description_upgrade_room,
        isAllowedInThread = false,
        isDevCommand = true,
    ),
    TABLE_FLIP(
        command = "/tableflip",
        parameters = "<message>",
        description = R.string.slash_command_description_table_flip,
    );

    val allAliases = listOf(command) + aliases.orEmpty()
    fun matches(inputCommand: CharSequence) = allAliases.any { it.contentEquals(inputCommand, true) }
    fun startsWith(input: CharSequence) =
        allAliases.any { it.startsWith(input, 1, true) }
}
