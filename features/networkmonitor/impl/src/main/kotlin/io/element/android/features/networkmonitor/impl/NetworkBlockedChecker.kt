/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:Suppress("DEPRECATION")

package io.element.android.features.networkmonitor.impl

import android.annotation.SuppressLint
import android.net.ConnectivityManager
import android.net.NetworkInfo

/**
 * Helper to synchronously check if the active network in [ConnectivityManager] is blocked.
 *
 * This is extracted to its own class because it uses deprecated APIs (but the only ones that are reliable)
 * and we don't want to suppress deprecations everywhere in the file this would be called.
 */
class NetworkBlockedChecker(
    private val connectivityManager: ConnectivityManager,
) {
    // The permission is granted by the manifest, false positive
    @SuppressLint("MissingPermission")
    fun isNetworkBlocked(): Boolean {
        // This call is deprecated, but it seems like it's the only reliable way to tell if doze has blocked network access
        return connectivityManager.activeNetworkInfo?.detailedState == NetworkInfo.DetailedState.BLOCKED
    }
}
