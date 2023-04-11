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

import android.content.Context
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.network.RetrofitFactory
import io.element.android.libraries.push.providers.unifiedpush.network.UnifiedPushApi
import io.element.android.services.toolbox.api.strings.StringProvider
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.net.URL
import javax.inject.Inject

class UnifiedPushHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val unifiedPushStore: UnifiedPushStore,
    private val stringProvider: StringProvider,
    private val retrofitFactory: RetrofitFactory,
    private val coroutineDispatchers: CoroutineDispatchers,
) {
    suspend fun storeCustomOrDefaultGateway(endpoint: String) {
        val gateway = UnifiedPushConfig.default_push_gateway_http_url
        val parsed = URL(endpoint)
        val custom = "${parsed.protocol}://${parsed.host}/_matrix/push/v1/notify"
        Timber.i("Testing $custom")
        try {
            withContext(coroutineDispatchers.io) {
                val api = retrofitFactory.create("${parsed.protocol}://${parsed.host}")
                    .create(UnifiedPushApi::class.java)
                tryOrNull { api.discover() }
                    ?.let { discoveryResponse ->
                        if (discoveryResponse.unifiedpush.gateway == "matrix") {
                            Timber.d("Using custom gateway")
                            unifiedPushStore.storePushGateway(custom)
                        }
                    }
            }
            return
        } catch (e: Throwable) {
            Timber.d(e, "Cannot try custom gateway")
        }
        unifiedPushStore.storePushGateway(gateway)
    }

    private fun isEmbeddedDistributor() = false

    fun getPrivacyFriendlyUpEndpoint(): String? {
        val endpoint = getEndpointOrToken()
        if (endpoint.isNullOrEmpty()) return null
        if (isEmbeddedDistributor()) {
            return endpoint
        }
        return try {
            val parsed = URL(endpoint)
            "${parsed.protocol}://${parsed.host}/***"
        } catch (e: Exception) {
            Timber.e(e, "Error parsing unifiedpush endpoint")
            null
        }
    }

    fun getEndpointOrToken(): String? {
        // TODO
        return if (isEmbeddedDistributor()) "" // fcmHelper.getFcmToken()
        else unifiedPushStore.getEndpoint()
    }

    fun getPushGateway(): String? {
        return if (isEmbeddedDistributor()) "" // PushConfig.pusher_http_url
        else unifiedPushStore.getPushGateway()
    }
}
