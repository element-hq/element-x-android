/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.timeline.item

import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.event.EventContent
import io.element.android.libraries.matrix.api.timeline.item.event.EventOrTransactionId
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileDetails

sealed interface EventThreadInfo {
    data class ThreadRoot(val summary: ThreadSummary) : EventThreadInfo
    data class ThreadResponse(val threadRootId: ThreadId) : EventThreadInfo
}

data class ThreadSummary(
    val latestEvent: AsyncData<EmbeddedEventInfo>,
    val numberOfReplies: Long,
)

data class EmbeddedEventInfo(
    val eventOrTransactionId: EventOrTransactionId,
    val content: EventContent,
    val senderId: UserId,
    val senderProfile: ProfileDetails,
    val timestamp: Long,
)
