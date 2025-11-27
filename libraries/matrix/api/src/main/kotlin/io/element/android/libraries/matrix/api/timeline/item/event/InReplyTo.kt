/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.timeline.item.event

import androidx.compose.runtime.Immutable
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId

@Immutable
sealed interface InReplyTo {
    /** The event details are not loaded yet. We can fetch them. */
    data class NotLoaded(val eventId: EventId) : InReplyTo

    /** The event details are pending to be fetched. We should **not** fetch them again. */
    data class Pending(val eventId: EventId) : InReplyTo

    /** The event details are available. */
    data class Ready(
        val eventId: EventId,
        val content: EventContent,
        val senderId: UserId,
        val senderProfile: ProfileDetails,
    ) : InReplyTo

    /**
     * Fetching the event details failed.
     *
     * We can try to fetch them again **with a proper retry strategy**, but not blindly:
     *
     * If the reason for the failure is consistent on the server, we'd enter a loop
     * where we keep trying to fetch the same event.
     * */
    data class Error(
        val eventId: EventId,
        val message: String,
    ) : InReplyTo
}
