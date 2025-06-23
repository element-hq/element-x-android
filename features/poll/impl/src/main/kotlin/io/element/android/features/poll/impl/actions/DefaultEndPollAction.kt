/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.impl.actions

import com.squareup.anvil.annotations.ContributesBinding
import im.vector.app.features.analytics.plan.PollEnd
import io.element.android.features.poll.api.actions.EndPollAction
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.services.analytics.api.AnalyticsService
import javax.inject.Inject

@ContributesBinding(RoomScope::class)
class DefaultEndPollAction @Inject constructor(
    private val room: JoinedRoom,
    private val analyticsService: AnalyticsService,
) : EndPollAction {
    override suspend fun execute(pollStartId: EventId): Result<Unit> {
        return room.liveTimeline.endPoll(
            pollStartId = pollStartId,
            text = "The poll with event id: $pollStartId has ended."
        ).onSuccess {
            analyticsService.capture(PollEnd())
        }
    }
}
