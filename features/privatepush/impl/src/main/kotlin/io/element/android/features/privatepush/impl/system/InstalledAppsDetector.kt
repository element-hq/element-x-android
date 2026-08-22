/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.privatepush.impl.system

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.pm.PackageInfoCompat
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.di.annotations.ApplicationContext

/** Answers "is this app installed?" without a Context, so presenters and tests can use it. */
interface InstalledAppsDetector {
    fun isInstalled(packageName: String): Boolean
    fun installedVersionCode(packageName: String): Long?
}

/** Requires the package to be visible: see the <queries> block of this module's manifest. */
@ContributesBinding(AppScope::class)
class DefaultInstalledAppsDetector(
    @ApplicationContext private val context: Context,
) : InstalledAppsDetector {
    override fun isInstalled(packageName: String): Boolean = installedVersionCode(packageName) != null

    override fun installedVersionCode(packageName: String): Long? = try {
        PackageInfoCompat.getLongVersionCode(context.packageManager.getPackageInfo(packageName, 0))
    } catch (_: PackageManager.NameNotFoundException) {
        null
    }
}
