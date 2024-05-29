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

package io.element.android.libraries.pushproviders.unifiedpush

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.AppScope
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.net.URL
import javax.inject.Inject

interface UnifiedPushGatewayResolver {
    suspend fun getGateway(endpoint: String): String
}

@ContributesBinding(AppScope::class)
class DefaultUnifiedPushGatewayResolver @Inject constructor(
    private val unifiedPushApiFactory: UnifiedPushApiFactory,
    private val coroutineDispatchers: CoroutineDispatchers,
) : UnifiedPushGatewayResolver {
    override suspend fun getGateway(endpoint: String): String {
        val gateway = UnifiedPushConfig.DEFAULT_PUSH_GATEWAY_HTTP_URL
        try {
            val url = URL(endpoint)
            val port = if (url.port != -1) ":${url.port}" else ""
            val customBase = "${url.protocol}://${url.host}$port"
            val customUrl = "$customBase/_matrix/push/v1/notify"
            Timber.i("Testing $customUrl")
            return withContext(coroutineDispatchers.io) {
                val api = unifiedPushApiFactory.create(customBase)
                try {
                    val discoveryResponse = api.discover()
                    if (discoveryResponse.unifiedpush.gateway == "matrix") {
                        Timber.d("Using custom gateway")
                        return@withContext customUrl
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
