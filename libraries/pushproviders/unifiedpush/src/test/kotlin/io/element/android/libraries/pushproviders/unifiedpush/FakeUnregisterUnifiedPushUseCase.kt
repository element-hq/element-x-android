/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.tests.testutils.lambda.lambdaError

class FakeUnregisterUnifiedPushUseCase(
    private val unregisterLambda: (MatrixClient, String, Boolean) -> Result<Unit> = { _, _, _ -> lambdaError() },
    private val cleanupLambda: (String, Boolean) -> Unit = { _, _ -> lambdaError() },
) : UnregisterUnifiedPushUseCase {
    override suspend fun unregister(
        matrixClient: MatrixClient,
        clientSecret: String,
        unregisterUnifiedPush: Boolean,
    ): Result<Unit> {
        return unregisterLambda(matrixClient, clientSecret, unregisterUnifiedPush)
    }

    override fun cleanup(
        clientSecret: String,
        unregisterUnifiedPush: Boolean,
    ) {
        cleanupLambda(clientSecret, unregisterUnifiedPush)
    }
}
