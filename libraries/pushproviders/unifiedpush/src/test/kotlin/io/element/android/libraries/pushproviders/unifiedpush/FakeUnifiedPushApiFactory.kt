/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
