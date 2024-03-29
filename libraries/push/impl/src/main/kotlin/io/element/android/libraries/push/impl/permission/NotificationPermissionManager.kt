/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.libraries.push.impl.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import io.element.android.libraries.di.ApplicationContext
import io.element.android.services.toolbox.api.sdk.BuildVersionSdkIntProvider
import javax.inject.Inject

// TODO EAx move
class NotificationPermissionManager @Inject constructor(
    private val sdkIntProvider: BuildVersionSdkIntProvider,
    @ApplicationContext private val context: Context,
) {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /*
    fun eventuallyRequestPermission(
            activity: Activity,
            requestPermissionLauncher: ActivityResultLauncher<Array<String>>,
            showRationale: Boolean = true,
            ignorePreference: Boolean = false,
    ) {
        if (!sdkIntProvider.isAtLeast(Build.VERSION_CODES.TIRAMISU)) return
        // if (!vectorPreferences.areNotificationEnabledForDevice() && !ignorePreference) return
        checkPermissions(
                listOf(Manifest.permission.POST_NOTIFICATIONS),
                activity,
                activityResultLauncher = requestPermissionLauncher,
                if (showRationale) R.string.permissions_rationale_msg_notification else 0
        )
    }
     */

    fun eventuallyRevokePermission(
        activity: Activity,
    ) {
        if (!sdkIntProvider.isAtLeast(Build.VERSION_CODES.TIRAMISU)) return
        activity.revokeSelfPermissionOnKill(Manifest.permission.POST_NOTIFICATIONS)
    }
}
