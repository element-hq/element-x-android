/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.api.pollcontent

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.timeline.item.event.EventTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.PollContent

/**
 * Turns the poll content of a timeline event into the state the poll UI renders.
 */
interface PollContentStateFactory {
    /**
     * Convenience overload that reads the editability and ownership from the timeline item itself.
     *
     * @param eventTimelineItem the timeline item carrying the poll.
     * @param content the poll content of that item.
     */
    suspend fun create(eventTimelineItem: EventTimelineItem, content: PollContent): PollContentState {
        return create(
            eventId = eventTimelineItem.eventId,
            isEditable = eventTimelineItem.isEditable,
            isOwn = eventTimelineItem.isOwn,
            content = content,
        )
    }

    /**
     * @param eventId the poll start event, or `null` while the poll is still a local echo.
     * @param isEditable whether the current user may still edit the poll.
     * @param isOwn whether the poll was created by the current user.
     * @param content the poll question, answers and votes.
     */
    suspend fun create(eventId: EventId?, isEditable: Boolean, isOwn: Boolean, content: PollContent): PollContentState
}
