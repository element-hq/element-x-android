/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.pinned.list

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.timeline.item.TimelineItemDebugInfo

interface PinnedMessagesListNavigator {
    fun viewInTimeline(eventId: EventId)
    fun navigateToEventDebugInfo(eventId: EventId?, debugInfo: TimelineItemDebugInfo)
    fun forwardEvent(eventId: EventId)
}
