/*
 * Copyright 2026 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model

import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEncryptedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLegacyCallInviteContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLocationContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRedactedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRtcNotificationContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStateContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemUnknownContent

/**
 * Whether this timeline event can be selected in multi-select ("selection") mode.
 */
fun TimelineItem.Event.canBeSelected(): Boolean {
    // Local echoes (no remote eventId) and failed sends are never selectable.
    if (!isRemote || failedToSend) return false
    return when (content) {
        is TimelineItemStateContent,
        is TimelineItemRedactedContent,
        is TimelineItemEncryptedContent,
        is TimelineItemLegacyCallInviteContent,
        is TimelineItemRtcNotificationContent,
        is TimelineItemLocationContent,
        is TimelineItemUnknownContent -> false
        else -> true
    }
}
