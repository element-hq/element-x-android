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
import io.element.android.libraries.push.impl.db.PushRequest
import io.element.android.tests.testutils.lambda.lambdaError
import kotlin.time.Instant

class FakePushHistoryService(
    private val onPushReceivedResult: (
        String,
        EventId?,
        RoomId?,
        SessionId?,
        Boolean,
        Boolean,
        String?
    ) -> Unit = { _, _, _, _, _, _, _ -> lambdaError() },
    private val enqueuePushRequest: (PushRequest) -> Result<Unit> = { lambdaError() },
    private val replacePushRequests: (List<PushRequest>) -> Result<Unit> = { lambdaError() },
    private val getPendingPushRequests: (SessionId, Instant?) -> Result<List<PushRequest>> = { _, _ -> lambdaError() },
    private val removeOldPushRequests: (SessionId) -> Result<Unit> = { lambdaError() },
) : PushHistoryService {
    override fun onPushResult(
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

    override suspend fun insertOrUpdatePushRequest(pushRequest: PushRequest): Result<Unit> {
        return enqueuePushRequest.invoke(pushRequest)
    }

    override suspend fun insertOrUpdatePushRequests(pushRequests: List<PushRequest>): Result<Unit> {
        return replacePushRequests.invoke(pushRequests)
    }

    override suspend fun getPendingPushRequests(sessionId: SessionId, since: Instant?): Result<List<PushRequest>> {
        return getPendingPushRequests.invoke(sessionId, since)
    }

    override suspend fun removeOldPushRequests(sessionId: SessionId): Result<Unit> {
        return removeOldPushRequests.invoke(sessionId)
    }
}
