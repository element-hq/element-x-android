/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.libraries.push.providers.unifiedpush

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.network.RetrofitFactory
import io.element.android.libraries.push.providers.unifiedpush.network.UnifiedPushApi
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.net.URL
import javax.inject.Inject

class UnifiedPushGatewayResolver @Inject constructor(
    private val retrofitFactory: RetrofitFactory,
    private val coroutineDispatchers: CoroutineDispatchers,
) {
    suspend fun getGateway(endpoint: String): String? {
        val gateway = UnifiedPushConfig.default_push_gateway_http_url
        val url = URL(endpoint)
        val custom = "${url.protocol}://${url.host}/_matrix/push/v1/notify"
        Timber.i("Testing $custom")
        try {
            return withContext(coroutineDispatchers.io) {
                val api = retrofitFactory.create("${url.protocol}://${url.host}")
                    .create(UnifiedPushApi::class.java)
                try {
                    val discoveryResponse = api.discover()
                    if (discoveryResponse.unifiedpush.gateway == "matrix") {
                        Timber.d("Using custom gateway")
                        return@withContext custom
                    }
                } catch (throwable: Throwable) {
                    Timber.tag("UnifiedPushHelper").e(throwable)
                }
                return@withContext gateway
            }
        } catch (e: Throwable) {
            Timber.d(e, "Cannot try custom gateway")
        }
        return gateway
    }
}
