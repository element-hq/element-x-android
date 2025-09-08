/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.impl.actions

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import im.vector.app.features.analytics.plan.PollEnd
import io.element.android.features.poll.api.actions.EndPollAction
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.services.analytics.api.AnalyticsService

@ContributesBinding(RoomScope::class)
@Inject
class DefaultEndPollAction(
    private val analyticsService: AnalyticsService,
) : EndPollAction {
    override suspend fun execute(timeline: Timeline, pollStartId: EventId): Result<Unit> {
        return timeline.endPoll(
            pollStartId = pollStartId,
            text = "The poll with event id: $pollStartId has ended."
        ).onSuccess {
            analyticsService.capture(PollEnd())
        }
    }
}
