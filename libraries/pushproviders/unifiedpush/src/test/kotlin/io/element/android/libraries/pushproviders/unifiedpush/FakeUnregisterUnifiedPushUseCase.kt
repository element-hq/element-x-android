/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.tests.testutils.lambda.lambdaError

class FakeUnregisterUnifiedPushUseCase(
    private val unregisterLambda: (MatrixClient, String) -> Result<Unit> = { _, _ -> lambdaError() },
    private val cleanupLambda: (String) -> Unit = { lambdaError() },
) : UnregisterUnifiedPushUseCase {
    override suspend fun unregister(matrixClient: MatrixClient, clientSecret: String): Result<Unit> {
        return unregisterLambda(matrixClient, clientSecret)
    }

    override fun cleanup(clientSecret: String) {
        cleanupLambda(clientSecret)
    }
}
