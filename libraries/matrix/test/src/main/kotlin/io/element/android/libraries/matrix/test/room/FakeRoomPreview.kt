/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.room

import androidx.compose.runtime.Immutable
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.room.RoomMembershipDetails
import io.element.android.libraries.matrix.api.room.RoomPreview
import io.element.android.libraries.matrix.api.room.preview.RoomPreviewInfo
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.simulateLongTask

@Immutable
class FakeRoomPreview(
    override val sessionId: SessionId = A_SESSION_ID,
    override val info: RoomPreviewInfo = aRoomPreviewInfo(),
    private val declineInviteResult: () -> Result<Unit> = { lambdaError() },
    private val forgetRoomResult: () -> Result<Unit> = { lambdaError() },
    private val roomMembershipDetails: () -> Result<RoomMembershipDetails?> = { lambdaError() },
) : RoomPreview {
    override suspend fun leave(): Result<Unit> = simulateLongTask {
        declineInviteResult()
    }

    override suspend fun forget(): Result<Unit> = simulateLongTask {
        forgetRoomResult()
    }

    override suspend fun membershipDetails(): Result<RoomMembershipDetails?> = simulateLongTask {
        roomMembershipDetails()
    }

    override fun close() = Unit
}
