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

import io.element.android.libraries.matrix.api.core.UserId

class FakeUnifiedPushStore(
    private val getEndpointResult: (String) -> String? = { TODO() },
    private val storeUpEndpointResult: (String, String?) -> Unit = { _, _ -> TODO() },
    private val getPushGatewayResult: (String) -> String? = { TODO() },
    private val storePushGatewayResult: (String, String?) -> Unit = { _, _ -> TODO() },
    private val getDistributorValueResult: (UserId) -> String? = { TODO() },
    private val setDistributorValueResult: (UserId, String) -> Unit = { _, _ -> TODO() },
) : UnifiedPushStore {
    override fun getEndpoint(clientSecret: String): String? {
        return getEndpointResult(clientSecret)
    }

    override fun storeUpEndpoint(clientSecret: String, endpoint: String?) {
        storeUpEndpointResult(clientSecret, endpoint)
    }

    override fun getPushGateway(clientSecret: String): String? {
        return getPushGatewayResult(clientSecret)
    }

    override fun storePushGateway(clientSecret: String, gateway: String?) {
        storePushGatewayResult(clientSecret, gateway)
    }

    override fun getDistributorValue(userId: UserId): String? {
        return getDistributorValueResult(userId)
    }

    override fun setDistributorValue(userId: UserId, value: String) {
        setDistributorValueResult(userId, value)
    }
}
