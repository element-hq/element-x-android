/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.core

import io.element.android.libraries.matrix.api.core.SendHandle
import io.element.android.tests.testutils.simulateLongTask

class FakeSendHandle(
    var retryLambda: () -> Result<Unit> = { Result.success(Unit) }
) : SendHandle {
    override suspend fun retry(): Result<Unit> = simulateLongTask {
        return retryLambda()
    }
}
