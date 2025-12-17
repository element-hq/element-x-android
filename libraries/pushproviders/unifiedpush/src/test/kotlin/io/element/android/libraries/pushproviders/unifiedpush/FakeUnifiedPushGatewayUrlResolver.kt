/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import io.element.android.tests.testutils.lambda.lambdaError

class FakeUnifiedPushGatewayUrlResolver(
    private val resolveResult: (UnifiedPushGatewayResolverResult, String) -> String = { _, _ -> lambdaError() },
) : UnifiedPushGatewayUrlResolver {
    override fun resolve(gatewayResult: UnifiedPushGatewayResolverResult, instance: String): String {
        return resolveResult(gatewayResult, instance)
    }
}
