/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.privatepush.impl.system

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.appconfig.PrivatePushConfig
import io.element.android.libraries.androidutils.system.openGooglePlay
import io.element.android.libraries.androidutils.system.openUrlInExternalApp
import io.element.android.libraries.di.annotations.ApplicationContext

/**
 * Opens other apps from a presenter. Everything goes through the application context with
 * FLAG_ACTIVITY_NEW_TASK: presenters run inside Molecule and must never read LocalContext.
 */
interface ExternalAppLauncher {
    /** Launch intent of an installed app; false when none. */
    fun openApp(packageName: String): Boolean
    fun openPlayStore(packageName: String)
    fun openFdroid(packageName: String)
}

@ContributesBinding(AppScope::class)
class DefaultExternalAppLauncher(
    @ApplicationContext private val context: Context,
    private val installedAppsDetector: InstalledAppsDetector,
) : ExternalAppLauncher {
    override fun openApp(packageName: String): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        return true
    }

    // Application context => openUrlInExternalApp adds FLAG_ACTIVITY_NEW_TASK itself and toasts on failure.
    override fun openPlayStore(packageName: String) = context.openGooglePlay(packageName)

    override fun openFdroid(packageName: String) {
        val store = PrivatePushConfig.FDROID_PACKAGES.firstOrNull(installedAppsDetector::isInstalled)
        val intent = Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri())
            .setPackage(store)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            context.openUrlInExternalApp(PrivatePushConfig.NTFY_FDROID_WEB_URL)
        }
    }
}
