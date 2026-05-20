/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.tasks

import kotlinx.coroutines.delay

class FakeMarkAllRoomsAsRead(
    private val invokeLambda: suspend () -> Result<MarkAllRoomsAsReadResult> = {
        delay(20L)
        Result.success(MarkAllRoomsAsReadResult(processedCount = 0, failedCount = 0))
    },
) : MarkAllRoomsAsRead {
    var invokeCallCount = 0
        private set

    override suspend fun invoke(): Result<MarkAllRoomsAsReadResult> {
        invokeCallCount++
        return invokeLambda()
    }
}
