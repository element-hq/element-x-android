/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.ui.utils.a11y

import android.app.Activity
import android.app.Application
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun hasExternalKeyboard(): Boolean {
    val activity = requireNotNull(LocalActivity.current)
    var hasExternalKeyboard by remember { mutableStateOf(activity.resources.configuration.keyboard != Configuration.KEYBOARD_NOKEYS) }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        DisposableEffect(Unit) {
            val callback = object : Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
                override fun onActivityStarted(activity: Activity) {}
                override fun onActivityResumed(activity: Activity) {
                    // We do not have access to onActivityConfigurationChanged, so update the value when tha Activity is resumed
                    hasExternalKeyboard = activity.resources.configuration.keyboard != Configuration.KEYBOARD_NOKEYS
                }

                override fun onActivityPaused(activity: Activity) {}
                override fun onActivityStopped(activity: Activity) {}
                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
                override fun onActivityDestroyed(activity: Activity) {}
            }
            activity.registerActivityLifecycleCallbacks(callback)
            onDispose {
                activity.unregisterActivityLifecycleCallbacks(callback)
            }
        }
    }
    return hasExternalKeyboard
}
