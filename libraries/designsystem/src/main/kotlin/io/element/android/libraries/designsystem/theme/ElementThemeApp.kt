/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.designsystem.theme

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.theme.Theme
import io.element.android.compound.theme.isDark
import io.element.android.compound.theme.mapToTheme
import io.element.android.libraries.preferences.api.store.AppPreferencesStore

/**
 * Theme to use for all the regular screens of the application.
 * Will manage the light / dark theme based on the user preference.
 * Will also ensure that the system is applying the correct global theme
 * to the application, especially when the system is light and the application
 * is forced to use dark theme.
 */
@Composable
fun ElementThemeApp(
    appPreferencesStore: AppPreferencesStore,
    content: @Composable () -> Unit,
) {
    val theme by remember {
        appPreferencesStore.getThemeFlow().mapToTheme()
    }
        .collectAsState(initial = Theme.System)
    LaunchedEffect(theme) {
        AppCompatDelegate.setDefaultNightMode(
            when (theme) {
                Theme.System -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                Theme.Light -> AppCompatDelegate.MODE_NIGHT_NO
                Theme.Dark -> AppCompatDelegate.MODE_NIGHT_YES
            }
        )
    }
    ElementTheme(
        darkTheme = theme.isDark(),
        content = content,
    )
}
