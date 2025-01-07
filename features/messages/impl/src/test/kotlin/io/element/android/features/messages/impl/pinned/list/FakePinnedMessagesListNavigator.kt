/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.pinned.list

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.timeline.item.TimelineItemDebugInfo

class FakePinnedMessagesListNavigator : PinnedMessagesListNavigator {
    var onViewInTimelineClickLambda: ((EventId) -> Unit)? = null
    override fun onViewInTimelineClick(eventId: EventId) {
        onViewInTimelineClickLambda?.invoke(eventId)
    }

    var onShowEventDebugInfoClickLambda: ((EventId?, TimelineItemDebugInfo) -> Unit)? = null
    override fun onShowEventDebugInfoClick(eventId: EventId?, debugInfo: TimelineItemDebugInfo) {
        onShowEventDebugInfoClickLambda?.invoke(eventId, debugInfo)
    }

    var onForwardEventClickLambda: ((EventId) -> Unit)? = null
    override fun onForwardEventClick(eventId: EventId) {
        onForwardEventClickLambda?.invoke(eventId)
    }
}
