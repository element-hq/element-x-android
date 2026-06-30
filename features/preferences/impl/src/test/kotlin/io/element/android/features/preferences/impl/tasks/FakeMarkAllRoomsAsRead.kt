/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.tasks

import io.element.android.tests.testutils.simulateLongTask

class FakeMarkAllRoomsAsRead(
    private val invokeLambda: suspend () -> Result<Unit> = { Result.success(Unit) },
) : MarkAllRoomsAsRead {
    var invokeCallCount = 0
        private set

    override suspend fun invoke(): Result<Unit> = simulateLongTask {
        invokeCallCount++
        invokeLambda()
    }
}
