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
 * Check if the app is currently locked.
 */
val LockScreenService.isLocked: Boolean
    get() = lockState.value == LockScreenLockState.Locked

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
