/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.media

import io.element.android.libraries.matrix.api.media.MediaUploadHandler
import io.element.android.tests.testutils.simulateLongTask
import kotlin.coroutines.cancellation.CancellationException

class FakeMediaUploadHandler(
    private var result: Result<Unit> = Result.success(Unit),
) : MediaUploadHandler {
    override suspend fun await(): Result<Unit> = simulateLongTask { result }

    override fun cancel() {
        result = Result.failure(CancellationException())
    }
}
