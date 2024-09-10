/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.call.test

import io.element.android.features.call.api.CallType
import io.element.android.features.call.api.ElementCallEntryPoint
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId

class FakeElementCallEntryPoint(
    var startCallResult: (CallType) -> Unit = {},
    var handleIncomingCallResult: (CallType.RoomCall, EventId, UserId, String?, String?, String?, String) -> Unit = { _, _, _, _, _, _, _ -> }
) : ElementCallEntryPoint {
    override fun startCall(callType: CallType) {
        startCallResult(callType)
    }

    override fun handleIncomingCall(
        callType: CallType.RoomCall,
        eventId: EventId,
        senderId: UserId,
        roomName: String?,
        senderName: String?,
        avatarUrl: String?,
        timestamp: Long,
        notificationChannelId: String
    ) {
        handleIncomingCallResult(callType, eventId, senderId, roomName, senderName, avatarUrl, notificationChannelId)
    }
}
