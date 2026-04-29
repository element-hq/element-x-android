/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.event

import io.element.android.libraries.matrix.api.notification.CallIntent
import io.element.android.libraries.matrix.api.timeline.item.event.EventType

// State of the call, for now only isDeclined but in the future could be missed, active.
sealed interface RtcNotificationState {
    /** Some users have declined, byMe indicates if the current user is one of them. */
    data class Declined(val byMe: Boolean) : RtcNotificationState

    object Started : RtcNotificationState
}

class TimelineItemRtcNotificationContent(
    val callIntent: CallIntent,
    val state: RtcNotificationState,
) : TimelineItemEventContent {
    override val type: String = EventType.RTC_NOTIFICATION
}
