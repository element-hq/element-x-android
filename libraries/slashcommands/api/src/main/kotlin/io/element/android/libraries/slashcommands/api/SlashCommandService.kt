/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.slashcommands.api

import io.element.android.libraries.matrix.api.timeline.Timeline

interface SlashCommandService {
    suspend fun getSuggestions(
        text: String,
        isInThread: Boolean,
    ): List<SlashCommandSuggestion>

    /**
     * Parse the message and return a SlashCommand.
     */
    suspend fun parse(
        textMessage: CharSequence,
        formattedMessage: String?,
        isInThreadTimeline: Boolean,
    ): SlashCommand

    /**
     * Proceed a SlashCommandSendMessage.
     */
    suspend fun proceedSendMessage(
        slashCommand: SlashCommand.SlashCommandSendMessage,
        timeline: Timeline,
    ): Result<Unit>

    /**
     * Proceed a SlashCommandAdmin.
     */
    suspend fun proceedAdmin(
        slashCommand: SlashCommand.SlashCommandAdmin,
    ): Result<Unit>
}
