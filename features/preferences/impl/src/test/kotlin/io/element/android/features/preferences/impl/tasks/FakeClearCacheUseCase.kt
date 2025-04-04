/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.tasks

import io.element.android.tests.testutils.simulateLongTask

class FakeClearCacheUseCase : ClearCacheUseCase {
    var executeHasBeenCalled = false
        private set

    override suspend fun invoke() = simulateLongTask {
        executeHasBeenCalled = true
    }
}
