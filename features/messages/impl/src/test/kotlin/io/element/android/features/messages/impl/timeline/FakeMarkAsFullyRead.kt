/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.tests.testutils.lambda.lambdaError

class FakeMarkAsFullyRead(
    private val invokeResult: (RoomId, EventId) -> Unit = { _, _ -> lambdaError() },
) : MarkAsFullyRead {
    override suspend fun invoke(roomId: RoomId, eventId: EventId): Result<Unit> {
        return runCatchingExceptions { invokeResult(roomId, eventId) }
    }
}
