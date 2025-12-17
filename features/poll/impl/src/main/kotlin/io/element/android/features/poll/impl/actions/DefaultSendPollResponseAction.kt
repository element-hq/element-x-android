/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.impl.actions

import dev.zacsweers.metro.ContributesBinding
import im.vector.app.features.analytics.plan.PollVote
import io.element.android.features.poll.api.actions.SendPollResponseAction
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.services.analytics.api.AnalyticsService

@ContributesBinding(RoomScope::class)
class DefaultSendPollResponseAction(
    private val analyticsService: AnalyticsService,
) : SendPollResponseAction {
    override suspend fun execute(timeline: Timeline, pollStartId: EventId, answerId: String): Result<Unit> {
        return timeline.sendPollResponse(
            pollStartId = pollStartId,
            answers = listOf(answerId),
        ).onSuccess {
            analyticsService.capture(PollVote())
        }
    }
}
