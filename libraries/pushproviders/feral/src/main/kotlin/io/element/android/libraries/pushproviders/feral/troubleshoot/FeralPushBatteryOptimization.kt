/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.feral.troubleshoot

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

/** Doze exemption: the socket only survives idle periods when Feral is exempt from battery optimisation. */
interface FeralPushBatteryOptimization {
    fun isIgnoringBatteryOptimizations(): Boolean

    /** Opens the system dialog asking to exempt Feral; returns false when no activity handles it. */
    fun requestIgnoringBatteryOptimizations(): Boolean
}

@ContributesBinding(AppScope::class)
class DefaultFeralPushBatteryOptimization(
    @ApplicationContext private val context: Context,
    private val externalIntentLauncher: ExternalIntentLauncher,
) : FeralPushBatteryOptimization {
    override fun isIgnoringBatteryOptimizations(): Boolean {
        return context.getSystemService<PowerManager>()?.isIgnoringBatteryOptimizations(context.packageName) == true
    }

    @SuppressLint("BatteryLife")
    override fun requestIgnoringBatteryOptimizations(): Boolean {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            .setData("package:${context.packageName}".toUri())
        return try {
            // The launcher adds FLAG_ACTIVITY_NEW_TASK (app context).
            externalIntentLauncher.launch(intent)
            true
        } catch (exception: ActivityNotFoundException) {
            Timber.w(exception, "Cannot open the battery optimisation dialog")
            false
        }
    }
}
