/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.slashcommands.test

import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.slashcommands.api.SlashCommand
import io.element.android.libraries.slashcommands.api.SlashCommandService
import io.element.android.libraries.slashcommands.api.SlashCommandSuggestion
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.simulateLongTask

class FakeSlashCommandService(
    private val getSuggestionsResult: (String, Boolean) -> List<SlashCommandSuggestion> = { _, _ -> lambdaError() },
    private val parseResult: (CharSequence, String?, Boolean) -> SlashCommand = { _, _, _ -> lambdaError() },
    private val proceedSendMessageResult: (SlashCommand.SlashCommandSendMessage, Timeline) -> Result<Unit> = { _, _ -> lambdaError() },
    private val proceedAdminResult: (SlashCommand.SlashCommandAdmin) -> Result<Unit> = { lambdaError() },
) : SlashCommandService {
    override suspend fun getSuggestions(text: String, isInThread: Boolean): List<SlashCommandSuggestion> = simulateLongTask {
        getSuggestionsResult(text, isInThread)
    }

    override suspend fun parse(
        textMessage: CharSequence,
        formattedMessage: String?,
        isInThreadTimeline: Boolean,
    ): SlashCommand = simulateLongTask {
        parseResult(textMessage, formattedMessage, isInThreadTimeline)
    }

    override suspend fun proceedSendMessage(
        slashCommand: SlashCommand.SlashCommandSendMessage,
        timeline: Timeline,
    ): Result<Unit> = simulateLongTask {
        proceedSendMessageResult(slashCommand, timeline)
    }

    override suspend fun proceedAdmin(slashCommand: SlashCommand.SlashCommandAdmin): Result<Unit> = simulateLongTask {
        proceedAdminResult(slashCommand)
    }
}
