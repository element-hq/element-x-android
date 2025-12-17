/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.test

import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.pushproviders.api.PusherSubscriber
import io.element.android.tests.testutils.lambda.lambdaError

class FakePusherSubscriber(
    private val registerPusherResult: (MatrixClient, String, String) -> Result<Unit> = { _, _, _ -> lambdaError() },
    private val unregisterPusherResult: (MatrixClient, String, String) -> Result<Unit> = { _, _, _ -> lambdaError() },
) : PusherSubscriber {
    override suspend fun registerPusher(matrixClient: MatrixClient, pushKey: String, gateway: String): Result<Unit> {
        return registerPusherResult(matrixClient, pushKey, gateway)
    }

    override suspend fun unregisterPusher(matrixClient: MatrixClient, pushKey: String, gateway: String): Result<Unit> {
        return unregisterPusherResult(matrixClient, pushKey, gateway)
    }
}
