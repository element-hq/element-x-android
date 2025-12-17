/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

interface UnifiedPushGatewayUrlResolver {
    fun resolve(
        gatewayResult: UnifiedPushGatewayResolverResult,
        instance: String,
    ): String
}

@ContributesBinding(AppScope::class)
class DefaultUnifiedPushGatewayUrlResolver(
    private val unifiedPushStore: UnifiedPushStore,
    private val defaultPushGatewayHttpUrlProvider: DefaultPushGatewayHttpUrlProvider,
) : UnifiedPushGatewayUrlResolver {
    override fun resolve(
        gatewayResult: UnifiedPushGatewayResolverResult,
        instance: String,
    ): String {
        return when (gatewayResult) {
            is UnifiedPushGatewayResolverResult.Error -> {
                // Use previous gateway if any, or the provided one
                unifiedPushStore.getPushGateway(instance) ?: gatewayResult.gateway
            }
            UnifiedPushGatewayResolverResult.ErrorInvalidUrl,
            UnifiedPushGatewayResolverResult.NoMatrixGateway -> {
                defaultPushGatewayHttpUrlProvider.provide()
            }
            is UnifiedPushGatewayResolverResult.Success -> {
                gatewayResult.gateway
            }
        }
    }
}
