/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.pip

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.di.annotations.ApplicationContext

interface PipSupportProvider {
    @ChecksSdkIntAtLeast(Build.VERSION_CODES.O)
    fun isPipSupported(): Boolean
}

@ContributesBinding(AppScope::class)
class DefaultPipSupportProvider(
    @ApplicationContext private val context: Context,
) : PipSupportProvider {
    override fun isPipSupported(): Boolean {
        val isSupportedByTheOs = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            context.packageManager?.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE).orFalse()
        return isSupportedByTheOs
    }
}
