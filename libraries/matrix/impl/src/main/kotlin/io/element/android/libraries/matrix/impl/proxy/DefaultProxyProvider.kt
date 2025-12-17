/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.proxy

import android.content.Context
import android.net.ConnectivityManager
import android.provider.Settings
import androidx.core.content.getSystemService
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.di.annotations.ApplicationContext
import timber.log.Timber

/**
 * Provides the proxy settings from the system.
 * Note that you can configure the global proxy using adb like this:
 * ```
 * adb shell settings put global http_proxy https://proxy.example.com:8080
 * ```
 * and to remove it:
 * ```
 * adb shell settings delete global http_proxy
 * ```
 */
@ContributesBinding(AppScope::class)
class DefaultProxyProvider(
    @ApplicationContext
    private val context: Context
) : ProxyProvider {
    override fun provides(): String? {
        val defaultProxy = context.getSystemService<ConnectivityManager>()?.defaultProxy
        if (defaultProxy == null) {
            // Note: can be tested by running:
            // adb shell settings put global http_proxy :0
            Timber.d("No default proxy")
            return null
        }
        return Settings.Global.getString(context.contentResolver, Settings.Global.HTTP_PROXY)
            ?.also {
                Timber.d("Using global proxy")
            }
    }
}
