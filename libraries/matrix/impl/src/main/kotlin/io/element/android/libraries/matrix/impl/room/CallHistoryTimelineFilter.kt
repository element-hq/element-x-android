/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import org.matrix.rustcomponents.sdk.FilterTimelineEventType
import org.matrix.rustcomponents.sdk.TimelineEventFilter
import uniffi.ruma_events.MessageLikeEventType

internal object CallHistoryTimelineFilter {
    fun create(): TimelineEventFilter = TimelineEventFilter.includeEventTypes(
        listOf(
            FilterTimelineEventType.MessageLike(MessageLikeEventType.CallNotify),
            FilterTimelineEventType.MessageLike(MessageLikeEventType.RtcNotification),
            FilterTimelineEventType.MessageLike(MessageLikeEventType.CallInvite),
        )
    )
}
