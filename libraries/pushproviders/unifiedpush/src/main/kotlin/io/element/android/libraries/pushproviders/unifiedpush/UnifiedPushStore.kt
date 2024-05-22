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
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.DefaultPreferences
import io.element.android.libraries.matrix.api.core.UserId
import javax.inject.Inject

class UnifiedPushStore @Inject constructor(
    @ApplicationContext val context: Context,
    @DefaultPreferences private val defaultPrefs: SharedPreferences,
) {
    /**
     * Retrieves the UnifiedPush Endpoint.
     *
     * @param clientSecret the client secret, to identify the session
     * @return the UnifiedPush Endpoint or null if not received
     */
    fun getEndpoint(clientSecret: String): String? {
        return defaultPrefs.getString(PREFS_ENDPOINT_OR_TOKEN + clientSecret, null)
    }

    /**
     * Store UnifiedPush Endpoint to the SharedPrefs.
     *
     * @param endpoint the endpoint to store
     * @param clientSecret the client secret, to identify the session
     */
    fun storeUpEndpoint(endpoint: String?, clientSecret: String) {
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
    fun getPushGateway(clientSecret: String): String? {
        return defaultPrefs.getString(PREFS_PUSH_GATEWAY + clientSecret, null)
    }

    /**
     * Store Push Gateway to the SharedPrefs.
     *
     * @param gateway the push gateway to store
     * @param clientSecret the client secret, to identify the session
     */
    fun storePushGateway(gateway: String?, clientSecret: String) {
        defaultPrefs.edit {
            putString(PREFS_PUSH_GATEWAY + clientSecret, gateway)
        }
    }

    fun getDistributorValue(userId: UserId): String? {
        return defaultPrefs.getString(PREFS_DISTRIBUTOR + userId, null)
    }

    fun setDistributorValue(userId: UserId, value: String) {
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
