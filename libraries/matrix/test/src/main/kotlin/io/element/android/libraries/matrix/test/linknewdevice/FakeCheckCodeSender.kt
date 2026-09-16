/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.linknewdevice

import io.element.android.libraries.matrix.api.linknewdevice.CheckCodeSender
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.simulateLongTask

class FakeCheckCodeSender(
    private val validateResult: (UByte) -> Boolean = { lambdaError() },
    private val sendResult: (UByte) -> Result<Unit> = { lambdaError() },
) : CheckCodeSender {
    override suspend fun validate(code: UByte): Boolean = simulateLongTask {
        validateResult(code)
    }

    override suspend fun send(code: UByte): Result<Unit> = simulateLongTask {
        sendResult(code)
    }
}
