/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.threads

import androidx.compose.runtime.Immutable
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.core.toThreadId
import io.element.android.libraries.matrix.api.timeline.item.event.EventContent
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileDetails

@Immutable
data class ThreadListItem(
    val rootEvent: ThreadListItemEvent,
    val latestEvent: ThreadListItemEvent?,
    val numberOfReplies: Long,
) {
    val threadId = rootEvent.eventId.toThreadId()
}

@Immutable
data class ThreadListItemEvent(
    val eventId: EventId,
    val senderId: UserId,
    val senderProfile: ProfileDetails,
    val isOwn: Boolean,
    val content: EventContent?,
    val timestamp: Long,
)
