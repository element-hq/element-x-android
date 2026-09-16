/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.test

import io.element.android.features.call.api.CallData
import io.element.android.features.call.api.ElementCallEntryPoint
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.tests.testutils.lambda.lambdaError

class FakeElementCallEntryPoint(
    var startCallResult: (CallData) -> Unit = { lambdaError() },
    var handleIncomingCallResult: (
        CallData,
        EventId,
        UserId,
        String?,
        String?,
        String?,
        String,
        String?,
    ) -> Unit = { _, _, _, _, _, _, _, _ -> lambdaError() }
) : ElementCallEntryPoint {
    override fun startCall(callData: CallData) {
        startCallResult(callData)
    }

    override suspend fun handleIncomingCall(
        callData: CallData,
        eventId: EventId,
        senderId: UserId,
        roomName: String?,
        senderName: String?,
        avatarUrl: String?,
        timestamp: Long,
        expirationTimestamp: Long,
        notificationChannelId: String,
        textContent: String?,
    ) {
        handleIncomingCallResult(
            callData,
            eventId,
            senderId,
            roomName,
            senderName,
            avatarUrl,
            notificationChannelId,
            textContent,
        )
    }
}
