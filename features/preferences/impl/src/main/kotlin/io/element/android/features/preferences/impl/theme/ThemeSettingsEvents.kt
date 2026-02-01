/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.theme

import io.element.android.compound.theme.Theme

sealed interface ThemeSettingsEvents {
    data class SetTheme(val theme: Theme) : ThemeSettingsEvents
    data class SetUseDynamicTheme(val useDynamicTheme: Boolean) : ThemeSettingsEvents
    data class SetCustomThemeColor(val color: Int?) : ThemeSettingsEvents
    data class SetWallpaper(val uri: String?) : ThemeSettingsEvents
    data class SetWallpaperDim(val dim: Boolean) : ThemeSettingsEvents
}
