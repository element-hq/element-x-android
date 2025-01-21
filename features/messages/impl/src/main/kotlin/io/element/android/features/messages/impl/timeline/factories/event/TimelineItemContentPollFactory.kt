/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.factories.event

import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemPollContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemUnknownContent
import io.element.android.features.poll.api.pollcontent.PollContentStateFactory
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.timeline.item.event.EventTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.PollContent
import javax.inject.Inject

class TimelineItemContentPollFactory @Inject constructor(
    private val featureFlagService: FeatureFlagService,
    private val pollContentStateFactory: PollContentStateFactory,
) {
    suspend fun create(
        event: EventTimelineItem,
        content: PollContent,
    ): TimelineItemEventContent {
        if (!featureFlagService.isFeatureEnabled(FeatureFlags.Polls)) return TimelineItemUnknownContent
        val pollContentState = pollContentStateFactory.create(event, content)
        return TimelineItemPollContent(
            isMine = pollContentState.isMine,
            isEditable = pollContentState.isPollEditable,
            eventId = event.eventId,
            question = pollContentState.question,
            answerItems = pollContentState.answerItems,
            pollKind = pollContentState.pollKind,
            isEnded = pollContentState.isPollEnded,
            isEdited = content.isEdited
        )
    }
}
