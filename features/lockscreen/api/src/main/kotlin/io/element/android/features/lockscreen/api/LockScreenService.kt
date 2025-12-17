/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.api

import android.os.Build
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

interface LockScreenService {
    /**
     * The current lock state of the app.
     */
    val lockState: StateFlow<LockScreenLockState>

    /**
     * Check if setting up the lock screen is required.
     * @return true if the lock screen is mandatory and not setup yet, false otherwise.
     */
    fun isSetupRequired(): Flow<Boolean>

    /**
     * Check if pin is setup.
     * @return true if the pin is setup, false otherwise.
     */
    fun isPinSetup(): Flow<Boolean>
}

/**
 * Makes sure the secure flag is set on the activity if the pin is setup.
 * @param activity the activity to set the flag on.
 */
fun LockScreenService.handleSecureFlag(activity: ComponentActivity) {
    isPinSetup()
        .onEach { isPinSetup ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                activity.setRecentsScreenshotEnabled(!isPinSetup)
            } else {
                if (isPinSetup) {
                    activity.window.setFlags(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE
                    )
                } else {
                    activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }
        }
        .launchIn(activity.lifecycleScope)
}
