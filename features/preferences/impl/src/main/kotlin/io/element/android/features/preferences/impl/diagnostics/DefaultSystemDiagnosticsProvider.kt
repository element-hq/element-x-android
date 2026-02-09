/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.diagnostics

import android.content.Context
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.di.annotations.ApplicationContext
import java.util.Locale
import java.util.TimeZone
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultSystemDiagnosticsProvider(
    @ApplicationContext private val context: Context,
) : DiagnosticsProvider {
    override suspend fun getDiagnostics(): Diagnostics {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val versionName = packageInfo.versionName.orEmpty().ifEmpty { "?" }
        val versionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
        val locale = Locale.getDefault().toLanguageTag()
        val timezone = TimeZone.getDefault().id
        val device = "${Build.MANUFACTURER} ${Build.MODEL}".trim()
        val os = "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})"

        return Diagnostics(
            entries = mapOf(
                "App version" to "$versionName ($versionCode)",
                "OS" to os,
                "Device" to device,
                "Locale" to locale,
                "Timezone" to timezone,
            ),
        )
    }
}
