/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import dev.zacsweers.metro.Inject
import io.element.android.compound.theme.Theme
import io.element.android.compound.theme.mapToTheme
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import kotlinx.coroutines.launch

@Inject
class ThemeSettingsPresenter(
    private val appPreferencesStore: AppPreferencesStore,
) : Presenter<ThemeSettingsState> {

    @Composable
    override fun present(): ThemeSettingsState {
        val coroutineScope = rememberCoroutineScope()
        
        val theme by appPreferencesStore
            .getThemeFlow()
            .mapToTheme()
            .collectAsState(initial = Theme.System)

        val useDynamicTheme by appPreferencesStore
            .getUseDynamicThemeFlow()
            .collectAsState(initial = true)

        val customThemeColor by appPreferencesStore
            .getCustomThemeColorFlow()
            .collectAsState(initial = null)

        val wallpaperUri by appPreferencesStore
            .getWallpaperFlow()
            .collectAsState(initial = null)

        val wallpaperDim by appPreferencesStore
            .getWallpaperDimFlow()
            .collectAsState(initial = false)

        fun handleEvent(event: ThemeSettingsEvents) {
            coroutineScope.launch {
                when (event) {
                    is ThemeSettingsEvents.SetTheme -> {
                        appPreferencesStore.setTheme(event.theme.name)
                    }
                    is ThemeSettingsEvents.SetUseDynamicTheme -> {
                        appPreferencesStore.setUseDynamicTheme(event.useDynamicTheme)
                    }
                    is ThemeSettingsEvents.SetCustomThemeColor -> {
                        appPreferencesStore.setCustomThemeColor(event.color)
                    }
                    is ThemeSettingsEvents.SetWallpaper -> {
                        appPreferencesStore.setWallpaper(event.uri)
                    }
                    is ThemeSettingsEvents.SetWallpaperDim -> {
                        appPreferencesStore.setWallpaperDim(event.dim)
                    }
                }
            }
        }

        return ThemeSettingsState(
            theme = theme,
            useDynamicTheme = useDynamicTheme,
            customThemeColor = customThemeColor,
            wallpaperUri = wallpaperUri,
            wallpaperDim = wallpaperDim,
            eventSink = ::handleEvent
        )
    }
}
