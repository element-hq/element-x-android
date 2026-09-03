/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.api.actions

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.timeline.Timeline

/**
 * Sends the current user's answer(s) to a poll, wrapping the timeline call so the analytics are captured in one place.
 */
interface SendPollResponseAction {
    /**
     * @param timeline the timeline the poll lives in.
     * @param pollStartId the poll start event being answered.
     * @param answerIds the ids of the chosen answers.
     */
    suspend fun execute(
        timeline: Timeline,
        pollStartId: EventId,
        answerIds: List<String>,
    ): Result<Unit>
}
