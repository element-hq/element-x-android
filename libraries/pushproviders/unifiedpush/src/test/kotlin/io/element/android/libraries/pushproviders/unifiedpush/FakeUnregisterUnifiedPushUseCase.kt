/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.tests.testutils.lambda.lambdaError

class FakeUnregisterUnifiedPushUseCase(
    private val result: (MatrixClient, String) -> Result<Unit> = { _, _ -> lambdaError() }
) : UnregisterUnifiedPushUseCase {
    override suspend fun execute(matrixClient: MatrixClient, clientSecret: String): Result<Unit> {
        return result(matrixClient, clientSecret)
    }
}
