/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import io.element.android.libraries.pushproviders.unifiedpush.network.DiscoveryResponse
import io.element.android.libraries.pushproviders.unifiedpush.network.UnifiedPushApi

class FakeUnifiedPushApiFactory(
    private val discoveryResponse: () -> DiscoveryResponse
) : UnifiedPushApiFactory {
    var baseUrlParameter: String? = null
        private set

    override fun create(baseUrl: String): UnifiedPushApi {
        baseUrlParameter = baseUrl
        return FakeUnifiedPushApi(discoveryResponse)
    }
}

class FakeUnifiedPushApi(
    private val discoveryResponse: () -> DiscoveryResponse
) : UnifiedPushApi {
    override suspend fun discover(): DiscoveryResponse {
        return discoveryResponse()
    }
}
