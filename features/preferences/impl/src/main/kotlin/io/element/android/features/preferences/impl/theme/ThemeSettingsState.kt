/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.theme

import io.element.android.compound.theme.Theme

data class ThemeSettingsState(
    val theme: Theme,
    val useDynamicTheme: Boolean,
    val customThemeColor: Int?,
    val wallpaperUri: String?,
    val wallpaperDim: Boolean,
    val eventSink: (ThemeSettingsEvents) -> Unit
)
