/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.tests.testutils.lambda.lambdaError

class FakeMarkAsFullyRead(
    private val invokeResult: (RoomId) -> Unit = { lambdaError() }
) : MarkAsFullyRead {
    override fun invoke(roomId: RoomId) {
        invokeResult(roomId)
    }
}
