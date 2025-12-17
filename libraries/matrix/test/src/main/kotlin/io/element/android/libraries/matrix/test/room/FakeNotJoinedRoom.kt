/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.room

import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.libraries.matrix.api.room.NotJoinedRoom
import io.element.android.libraries.matrix.api.room.RoomMembershipDetails
import io.element.android.libraries.matrix.api.room.preview.RoomPreviewInfo
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.simulateLongTask

class FakeNotJoinedRoom(
    override val localRoom: BaseRoom? = null,
    override val previewInfo: RoomPreviewInfo = aRoomPreviewInfo(),
    private val roomMembershipDetails: () -> Result<RoomMembershipDetails?> = { lambdaError() },
) : NotJoinedRoom {
    override suspend fun membershipDetails(): Result<RoomMembershipDetails?> = simulateLongTask {
        roomMembershipDetails()
    }

    override fun close() = Unit
}
