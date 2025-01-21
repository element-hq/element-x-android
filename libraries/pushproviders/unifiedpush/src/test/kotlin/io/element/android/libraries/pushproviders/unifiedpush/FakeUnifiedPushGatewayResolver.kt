/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import io.element.android.tests.testutils.lambda.lambdaError

class FakeUnifiedPushGatewayResolver(
    private val getGatewayResult: (String) -> UnifiedPushGatewayResolverResult = { lambdaError() },
) : UnifiedPushGatewayResolver {
    override suspend fun getGateway(endpoint: String): UnifiedPushGatewayResolverResult {
        return getGatewayResult(endpoint)
    }
}
