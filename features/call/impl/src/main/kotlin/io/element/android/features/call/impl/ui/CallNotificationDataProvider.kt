/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.ui

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.call.impl.notifications.CallNotificationData
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId

open class CallNotificationDataProvider : PreviewParameterProvider<CallNotificationData> {
    override val values: Sequence<CallNotificationData>
        get() = sequenceOf(
            aCallNotificationData(
                audioOnly = false
            ),
            aCallNotificationData(
                audioOnly = true
            ),
        )
}

internal fun aCallNotificationData(
    audioOnly: Boolean
): CallNotificationData {
    return CallNotificationData(
        sessionId = SessionId("@alice:matrix.org"),
        roomId = RoomId("!1234:matrix.org"),
        eventId = EventId("\$asdadadsad:matrix.org"),
        senderId = UserId("@bob:matrix.org"),
        roomName = "A room",
        senderName = "Bob",
        avatarUrl = null,
        notificationChannelId = "incoming_call",
        timestamp = 0L,
        textContent = null,
        expirationTimestamp = 1000L,
        audioOnly = audioOnly
    )
}
