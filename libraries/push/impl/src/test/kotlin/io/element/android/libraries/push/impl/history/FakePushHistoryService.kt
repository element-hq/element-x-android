/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.history

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.tests.testutils.lambda.lambdaError

class FakePushHistoryService(
    private val onPushReceivedResult: (
        String,
        EventId?,
        RoomId?,
        SessionId?,
        Boolean,
        Boolean,
        String?
    ) -> Unit = { _, _, _, _, _, _, _ -> lambdaError() }
) : PushHistoryService {
    override fun onPushReceived(
        providerInfo: String,
        eventId: EventId?,
        roomId: RoomId?,
        sessionId: SessionId?,
        hasBeenResolved: Boolean,
        includeDeviceState: Boolean,
        comment: String?,
    ) {
        onPushReceivedResult(
            providerInfo,
            eventId,
            roomId,
            sessionId,
            hasBeenResolved,
            includeDeviceState,
            comment
        )
    }
}
