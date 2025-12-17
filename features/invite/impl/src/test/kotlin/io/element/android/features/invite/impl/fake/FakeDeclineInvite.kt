/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invite.impl.fake

import io.element.android.features.invite.impl.DeclineInvite
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.simulateLongTask

class FakeDeclineInvite(
    private val lambda: (RoomId, Boolean, Boolean, String?) -> Result<RoomId> = { _, _, _, _ -> lambdaError() },
) : DeclineInvite {
    override suspend fun invoke(roomId: RoomId, blockUser: Boolean, reportRoom: Boolean, reportReason: String?): Result<RoomId> = simulateLongTask {
        lambda(roomId, blockUser, reportRoom, reportReason)
    }
}
