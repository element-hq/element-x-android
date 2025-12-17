/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.compat

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build

fun PackageManager.queryIntentActivitiesCompat(data: Intent, flags: Int): List<ResolveInfo> {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> queryIntentActivities(
            data,
            PackageManager.ResolveInfoFlags.of(flags.toLong())
        )
        else -> @Suppress("DEPRECATION") queryIntentActivities(data, flags)
    }
}

fun PackageManager.getApplicationInfoCompat(packageName: String, flags: Int): ApplicationInfo {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getApplicationInfo(
            packageName,
            PackageManager.ApplicationInfoFlags.of(flags.toLong())
        )
        else -> getApplicationInfo(packageName, flags)
    }
}
