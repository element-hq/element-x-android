/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import io.element.android.tests.testutils.lambda.lambdaError

class FakeUnifiedPushGatewayResolver(
    private val getGatewayResult: (String) -> String = { lambdaError() },
) : UnifiedPushGatewayResolver {
    override suspend fun getGateway(endpoint: String): String {
        return getGatewayResult(endpoint)
    }
}
