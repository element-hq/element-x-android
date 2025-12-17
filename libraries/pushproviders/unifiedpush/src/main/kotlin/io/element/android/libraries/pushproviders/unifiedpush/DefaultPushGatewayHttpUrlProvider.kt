/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.enterprise.api.EnterpriseService

interface DefaultPushGatewayHttpUrlProvider {
    fun provide(): String
}

@ContributesBinding(AppScope::class)
class DefaultDefaultPushGatewayHttpUrlProvider(
    private val enterpriseService: EnterpriseService,
) : DefaultPushGatewayHttpUrlProvider {
    override fun provide(): String {
        return enterpriseService.unifiedPushDefaultPushGateway() ?: UnifiedPushConfig.DEFAULT_PUSH_GATEWAY_HTTP_URL
    }
}
