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

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.DefaultPreferences
import io.element.android.libraries.matrix.api.core.UserId
import javax.inject.Inject

interface UnifiedPushStore {
    fun getEndpoint(clientSecret: String): String?
    fun storeUpEndpoint(clientSecret: String, endpoint: String?)
    fun getPushGateway(clientSecret: String): String?
    fun storePushGateway(clientSecret: String, gateway: String?)
    fun getDistributorValue(userId: UserId): String?
    fun setDistributorValue(userId: UserId, value: String)
}

@ContributesBinding(AppScope::class)
class DefaultUnifiedPushStore @Inject constructor(
    @ApplicationContext val context: Context,
    @DefaultPreferences private val defaultPrefs: SharedPreferences,
) : UnifiedPushStore {
    /**
     * Retrieves the UnifiedPush Endpoint.
     *
     * @param clientSecret the client secret, to identify the session
     * @return the UnifiedPush Endpoint or null if not received
     */
    override fun getEndpoint(clientSecret: String): String? {
        return defaultPrefs.getString(PREFS_ENDPOINT_OR_TOKEN + clientSecret, null)
    }

    /**
     * Store UnifiedPush Endpoint to the SharedPrefs.
     *
     * @param clientSecret the client secret, to identify the session
     * @param endpoint the endpoint to store
     */
    override fun storeUpEndpoint(clientSecret: String, endpoint: String?) {
        defaultPrefs.edit {
            putString(PREFS_ENDPOINT_OR_TOKEN + clientSecret, endpoint)
        }
    }

    /**
     * Retrieves the Push Gateway.
     *
     * @param clientSecret the client secret, to identify the session
     * @return the Push Gateway or null if not defined
     */
    override fun getPushGateway(clientSecret: String): String? {
        return defaultPrefs.getString(PREFS_PUSH_GATEWAY + clientSecret, null)
    }

    /**
     * Store Push Gateway to the SharedPrefs.
     *
     * @param clientSecret the client secret, to identify the session
     * @param gateway the push gateway to store
     */
    override fun storePushGateway(clientSecret: String, gateway: String?) {
        defaultPrefs.edit {
            putString(PREFS_PUSH_GATEWAY + clientSecret, gateway)
        }
    }

    override fun getDistributorValue(userId: UserId): String? {
        return defaultPrefs.getString(PREFS_DISTRIBUTOR + userId, null)
    }

    override fun setDistributorValue(userId: UserId, value: String) {
        defaultPrefs.edit {
            putString(PREFS_DISTRIBUTOR + userId, value)
        }
    }

    companion object {
        private const val PREFS_ENDPOINT_OR_TOKEN = "UP_ENDPOINT_OR_TOKEN"
        private const val PREFS_PUSH_GATEWAY = "PUSH_GATEWAY"
        private const val PREFS_DISTRIBUTOR = "DISTRIBUTOR"
    }
}
