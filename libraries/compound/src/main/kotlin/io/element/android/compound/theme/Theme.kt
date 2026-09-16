/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.compound.theme

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class Theme {
    System,
    Dark,
    Black,
    Light,
}

private fun Theme.coerceBlackTheme(allowBlackTheme: Boolean): Theme {
    return if (this == Theme.Black && !allowBlackTheme) Theme.Dark else this
}

@Composable
fun Theme.isDark(): Boolean {
    return when (this) {
        Theme.System -> isSystemThemeDark()
        Theme.Dark, Theme.Black -> true
        Theme.Light -> false
    }
}

/**
 * Whether the system-wide theme is dark.
 *
 * This deliberately does not use [androidx.compose.foundation.isSystemInDarkTheme], which reads the activity configuration: `AppCompatDelegate` rewrites
 * that configuration whenever a non-system theme is selected, so it keeps reporting the previously forced value. The application configuration is left
 * untouched by AppCompat and is what AppCompat itself reads to expand `MODE_NIGHT_FOLLOW_SYSTEM`.
 */
@Composable
private fun isSystemThemeDark(): Boolean {
    val applicationContext = LocalContext.current.applicationContext
    val activityConfiguration = LocalConfiguration.current
    return remember(activityConfiguration) {
        applicationContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }
}

fun Flow<String?>.mapToTheme(allowBlackTheme: Boolean): Flow<Theme> = map {
    when (it) {
        null -> Theme.System
        else -> Theme.valueOf(it)
    }.coerceBlackTheme(allowBlackTheme)
}
