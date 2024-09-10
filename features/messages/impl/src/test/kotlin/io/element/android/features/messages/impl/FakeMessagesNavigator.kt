/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.TimelineItemDebugInfo

class FakeMessagesNavigator : MessagesNavigator {
    var onShowEventDebugInfoClickedCount = 0
        private set

    var onForwardEventClickedCount = 0
        private set

    var onReportContentClickedCount = 0
        private set

    var onEditPollClickedCount = 0
        private set

    override fun onShowEventDebugInfoClick(eventId: EventId?, debugInfo: TimelineItemDebugInfo) {
        onShowEventDebugInfoClickedCount++
    }

    override fun onForwardEventClick(eventId: EventId) {
        onForwardEventClickedCount++
    }

    override fun onReportContentClick(eventId: EventId, senderId: UserId) {
        onReportContentClickedCount++
    }

    override fun onEditPollClick(eventId: EventId) {
        onEditPollClickedCount++
    }
}
