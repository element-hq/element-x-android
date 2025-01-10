/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.room

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.room.RoomPreview
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.simulateLongTask

class FakeRoomPreview(
    override val sessionId: SessionId = A_SESSION_ID,
    override val roomId: RoomId = A_ROOM_ID,
    private val declineInviteResult: () -> Result<Unit> = { lambdaError() },
    private val forgetRoomResult: () -> Result<Unit> = { lambdaError() },
) : RoomPreview {
    override suspend fun leave(): Result<Unit> = simulateLongTask {
        declineInviteResult()
    }

    override suspend fun forget(): Result<Unit> = simulateLongTask {
        forgetRoomResult()
    }

    override fun close() = Unit
}
