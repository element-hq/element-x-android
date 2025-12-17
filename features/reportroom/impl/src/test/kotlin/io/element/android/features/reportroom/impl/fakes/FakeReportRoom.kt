/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.reportroom.impl.fakes

import io.element.android.features.reportroom.impl.ReportRoom
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.simulateLongTask

class FakeReportRoom(
    var lambda: (RoomId, Boolean, String, Boolean) -> Result<Unit> = { _, _, _, _ -> lambdaError() }
) : ReportRoom {
    override suspend fun invoke(
        roomId: RoomId,
        shouldReport: Boolean,
        reason: String,
        shouldLeave: Boolean
    ): Result<Unit> = simulateLongTask {
        lambda(roomId, shouldReport, reason, shouldLeave)
    }
}
