/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import io.element.android.tests.testutils.lambda.lambdaError

class FakeUnifiedPushNewGatewayHandler(
    private val handleResult: (String, String, String) -> Result<Unit> = { _, _, _ -> lambdaError() },
) : UnifiedPushNewGatewayHandler {
    override suspend fun handle(endpoint: String, pushGateway: String, clientSecret: String): Result<Unit> {
        return handleResult(endpoint, pushGateway, clientSecret)
    }
}
