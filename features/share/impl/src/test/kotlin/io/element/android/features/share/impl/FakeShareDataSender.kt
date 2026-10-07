/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.share.impl

import io.element.android.features.share.api.ShareIntentData
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.tests.testutils.lambda.lambdaError

class FakeShareDataSender(
    private val sendResult: (MatrixClient, ShareIntentData, List<RoomId>) -> Result<Unit> = { _, _, _ -> lambdaError() },
) : ShareDataSender {
    override suspend fun send(client: MatrixClient, shareIntentData: ShareIntentData, roomIds: List<RoomId>): Result<Unit> {
        return sendResult(client, shareIntentData, roomIds)
    }
}
