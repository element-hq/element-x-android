/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

interface DefaultPushGatewayHttpUrlProvider {
    fun provide(): String
}

@ContributesBinding(AppScope::class)
class DefaultDefaultPushGatewayHttpUrlProvider @Inject constructor(
    private val enterpriseService: EnterpriseService,
) : DefaultPushGatewayHttpUrlProvider {
    override fun provide(): String {
        return enterpriseService.unifiedPushDefaultPushGateway() ?: UnifiedPushConfig.DEFAULT_PUSH_GATEWAY_HTTP_URL
    }
}
