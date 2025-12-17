/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.battery

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.services.toolbox.api.intent.ExternalIntentLauncher
import timber.log.Timber

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
     * @return true if the intent was successfully started, false if the activity was not found
     */
    fun requestDisablingBatteryOptimization(): Boolean
}

@ContributesBinding(AppScope::class)
class AndroidBatteryOptimization(
    @ApplicationContext
    private val context: Context,
    private val externalIntentLauncher: ExternalIntentLauncher,
) : BatteryOptimization {
    override fun isIgnoringBatteryOptimizations(): Boolean {
        return context.getSystemService<PowerManager>()
            ?.isIgnoringBatteryOptimizations(context.packageName) == true
    }

    @SuppressLint("BatteryLife")
    override fun requestDisablingBatteryOptimization(): Boolean {
        val ignoreBatteryOptimizationsResult = launchAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, withData = true)
        if (ignoreBatteryOptimizationsResult) {
            return true
        }
        // Open settings as a fallback if the first attempt fails
        return launchAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS, withData = false)
    }

    private fun launchAction(
        action: String,
        withData: Boolean,
    ): Boolean {
        val intent = Intent()
        intent.action = action
        if (withData) {
            intent.data = ("package:" + context.packageName).toUri()
        }
        return try {
            externalIntentLauncher.launch(intent)
            true
        } catch (exception: ActivityNotFoundException) {
            Timber.w(exception, "Cannot launch intent with action $action.")
            false
        }
    }
}
