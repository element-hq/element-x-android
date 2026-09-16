/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.slashcommands.api

import io.element.android.libraries.matrix.api.timeline.Timeline

/**
 * Recognises and runs the slash commands the user can type in the composer, such as `/me` or `/join`.
 *
 * The composer first [parse]s what was typed, then hands the result to whichever proceed method matches its kind; navigation
 * commands and the error cases are handled by the composer itself.
 */
interface SlashCommandService {
    /**
     * Returns the commands whose name starts with what has been typed, so the composer can offer an autocomplete list.
     *
     * @param text the current composer content.
     * @param isInThread whether the composer is in a thread, since not every command is available there.
     */
    suspend fun getSuggestions(
        text: String,
        isInThread: Boolean,
    ): List<SlashCommandSuggestion>

    /**
     * Parse the message and return a SlashCommand.
     * Ordinary text comes back as `NotACommand`, and a malformed or unsupported command as one of the error cases, so this never throws.
     *
     * @param textMessage the composer content as plain text.
     * @param formattedMessage the composer content as HTML, or `null` when unformatted.
     * @param isInThreadTimeline whether the composer is in a thread.
     */
    suspend fun parse(
        textMessage: CharSequence,
        formattedMessage: String?,
        isInThreadTimeline: Boolean,
    ): SlashCommand

    /**
     * Proceed a SlashCommandSendMessage.
     *
     * @param slashCommand the parsed command, carrying the content to send.
     * @param timeline the timeline to send it to.
     */
    suspend fun proceedSendMessage(
        slashCommand: SlashCommand.SlashCommandSendMessage,
        timeline: Timeline,
    ): Result<Unit>

    /**
     * Proceed a SlashCommandAdmin, i.e. one that acts on the room or its members rather than sending a message.
     *
     * @param slashCommand the parsed command to run.
     */
    suspend fun proceedAdmin(
        slashCommand: SlashCommand.SlashCommandAdmin,
    ): Result<Unit>
}
