/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

interface UnifiedPushGatewayUrlResolver {
    fun resolve(
        gatewayResult: UnifiedPushGatewayResolverResult,
        instance: String,
    ): String
}

@ContributesBinding(AppScope::class)
class DefaultUnifiedPushGatewayUrlResolver @Inject constructor(
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
