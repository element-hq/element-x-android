/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.audio

import android.content.Context
import android.os.PowerManager
import androidx.core.content.getSystemService

class WebViewProximitySensor(
    context: Context,
) {
    private val proximitySensorWakeLock by lazy {
        context.getSystemService<PowerManager>()
            ?.takeIf { it.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK) }
            ?.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "${context.packageName}:ProximitySensorCallWakeLock")
    }

    fun acquire() {
        if (proximitySensorWakeLock?.isHeld == false) {
            proximitySensorWakeLock?.acquire(2 * 60 * 60 * 1000L) // Acquire for 2 hours
        }
    }

    fun release() {
        if (proximitySensorWakeLock?.isHeld == true) {
            proximitySensorWakeLock?.release()
        }
    }
}
