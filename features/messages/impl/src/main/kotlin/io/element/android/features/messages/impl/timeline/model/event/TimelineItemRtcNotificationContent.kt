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

// For now this is just an enum, but could be a
// sealed class if we need the list of users who declined.
enum class RtcNotificationState {
    /** Some users have declined */
     Declined,

    /** I have declined this call */
    DeclinedByMe,

    // Future sates could be `Missed`? `ongoing`...
    None
}

class TimelineItemRtcNotificationContent(
    val callIntent: CallIntent,
    val state: RtcNotificationState,
) : TimelineItemEventContent {
    override val type: String = EventType.RTC_NOTIFICATION
}
