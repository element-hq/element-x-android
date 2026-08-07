/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.compound.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
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
        Theme.System -> isSystemInDarkTheme()
        Theme.Dark, Theme.Black -> true
        Theme.Light -> false
    }
}

fun Flow<String?>.mapToTheme(allowBlackTheme: Boolean): Flow<Theme> = map {
    when (it) {
        null -> Theme.System
        else -> Theme.valueOf(it)
    }.coerceBlackTheme(allowBlackTheme)
}
