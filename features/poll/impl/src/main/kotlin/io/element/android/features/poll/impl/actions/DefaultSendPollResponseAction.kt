/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.impl.actions

import com.squareup.anvil.annotations.ContributesBinding
import im.vector.app.features.analytics.plan.PollVote
import io.element.android.features.poll.api.actions.SendPollResponseAction
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.services.analytics.api.AnalyticsService
import javax.inject.Inject

@ContributesBinding(RoomScope::class)
class DefaultSendPollResponseAction @Inject constructor(
    private val room: JoinedRoom,
    private val analyticsService: AnalyticsService,
) : SendPollResponseAction {
    override suspend fun execute(pollStartId: EventId, answerId: String): Result<Unit> {
        return room.liveTimeline.sendPollResponse(
            pollStartId = pollStartId,
            answers = listOf(answerId),
        ).onSuccess {
            analyticsService.capture(PollVote())
        }
    }
}
