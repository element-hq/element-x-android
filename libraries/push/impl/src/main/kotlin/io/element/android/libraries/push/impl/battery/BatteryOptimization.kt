/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.battery

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

interface BatteryOptimization {
    /**
     * Tells if the application ignores battery optimizations.
     *
     * Ignoring them allows the app to run in background to make background sync with the homeserver.
     * This user option appears on Android M but Android O enforces its usage and kills apps not
     * authorised by the user to run in background.
     *
     * @return true if battery optimisations are ignored
     */
    fun isIgnoringBatteryOptimizations(): Boolean

    /**
     * Request the user to disable battery optimizations for this app.
     * This will open the system settings where the user can disable battery optimizations.
     * See https://developer.android.com/training/monitoring-device-state/doze-standby#exemption-cases
     *
     * @param activity The activity from which to start the settings intent.
     * @return true if the intent was successfully started, false if the activity was not found
     */
    fun requestDisablingBatteryOptimization(activity: Activity?): Boolean
}

@ContributesBinding(AppScope::class)
class AndroidBatteryOptimization @Inject constructor(
    @ApplicationContext
    private val context: Context,
) : BatteryOptimization {
    override fun isIgnoringBatteryOptimizations(): Boolean {
        return context.getSystemService<PowerManager>()
            ?.isIgnoringBatteryOptimizations(context.packageName) == true
    }

    @SuppressLint("BatteryLife")
    override fun requestDisablingBatteryOptimization(activity: Activity?): Boolean {
        activity ?: return false
        val intent = Intent()
        intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        intent.data = ("package:" + context.packageName).toUri()
        return try {
            activity.startActivity(intent)
            true
        } catch (exception: ActivityNotFoundException) {
            Timber.w(exception, "Cannot request ignoring battery optimizations.")
            false
        }
    }
}
