/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.core.UserId

interface UnifiedPushStore {
    fun getEndpoint(clientSecret: String): String?
    fun storeUpEndpoint(clientSecret: String, endpoint: String?)
    fun getPushGateway(clientSecret: String): String?
    fun storePushGateway(clientSecret: String, gateway: String?)
    fun getDistributorValue(userId: UserId): String?
    fun setDistributorValue(userId: UserId, value: String)
}

@ContributesBinding(AppScope::class)
class SharedPreferencesUnifiedPushStore(
    @ApplicationContext val context: Context,
    private val sharedPreferences: SharedPreferences,
) : UnifiedPushStore {
    /**
     * Retrieves the UnifiedPush Endpoint.
     *
     * @param clientSecret the client secret, to identify the session
     * @return the UnifiedPush Endpoint or null if not received
     */
    override fun getEndpoint(clientSecret: String): String? {
        return sharedPreferences.getString(PREFS_ENDPOINT_OR_TOKEN + clientSecret, null)
    }

    /**
     * Store UnifiedPush Endpoint to the SharedPrefs.
     *
     * @param clientSecret the client secret, to identify the session
     * @param endpoint the endpoint to store
     */
    override fun storeUpEndpoint(clientSecret: String, endpoint: String?) {
        sharedPreferences.edit {
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
        return sharedPreferences.getString(PREFS_PUSH_GATEWAY + clientSecret, null)
    }

    /**
     * Store Push Gateway to the SharedPrefs.
     *
     * @param clientSecret the client secret, to identify the session
     * @param gateway the push gateway to store
     */
    override fun storePushGateway(clientSecret: String, gateway: String?) {
        sharedPreferences.edit {
            putString(PREFS_PUSH_GATEWAY + clientSecret, gateway)
        }
    }

    override fun getDistributorValue(userId: UserId): String? {
        return sharedPreferences.getString(PREFS_DISTRIBUTOR + userId, null)
    }

    override fun setDistributorValue(userId: UserId, value: String) {
        sharedPreferences.edit {
            putString(PREFS_DISTRIBUTOR + userId, value)
        }
    }

    companion object {
        private const val PREFS_ENDPOINT_OR_TOKEN = "UP_ENDPOINT_OR_TOKEN"
        private const val PREFS_PUSH_GATEWAY = "PUSH_GATEWAY"
        private const val PREFS_DISTRIBUTOR = "DISTRIBUTOR"
    }
}
