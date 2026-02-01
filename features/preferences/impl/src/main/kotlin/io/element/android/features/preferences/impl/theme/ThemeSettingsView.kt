/*
* Copyright (c) 2025 Element Creations Ltd.
*
* SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
* Please see LICENSE files in the repository root for full details.
*/

package io.element.android.features.preferences.impl.theme

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.Theme
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.components.preferences.PreferencePage
import io.element.android.libraries.designsystem.components.preferences.PreferenceSwitch
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarHost
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.toImmutableList

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext

@Composable
fun ThemeSettingsView(
    state: ThemeSettingsState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val wallpaperLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            try {
                context.contentResolver.takePersistableUriPermission(it, flags)
            } catch (e: Exception) {
                // In some cases (e.g. if file is not local) this might fail or not be needed
                // We proceed anyway
            }
            state.eventSink(ThemeSettingsEvents.SetWallpaper(it.toString()))
        }
    }

    PreferencePage(
        modifier = modifier,
        onBackClick = onBackClick,
        title = "Appearance",
        snackbarHost = {}
    ) {
        // Theme Mode
        ThemeModeSection(
            theme = state.theme,
            onThemeSelected = { state.eventSink(ThemeSettingsEvents.SetTheme(it)) }
        )

        // Dynamic Colors (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PreferenceSwitch(
                title = "Use system colors", // TODO: Add to strings
                isChecked = state.useDynamicTheme,
                onCheckedChange = { state.eventSink(ThemeSettingsEvents.SetUseDynamicTheme(it)) },
                subtitle = "Apply the system's color palette to the app." // TODO: Add to strings
            )
        }
        
        // Custom Color (Only if Dynamic is disabled)
        if (!state.useDynamicTheme) {
             ListItem(
                headlineContent = { Text("Custom Theme Color") },
                supportingContent = { Text("Applies a custom seed color to the theme.") },
             )
        }

        // Wallpaper Section
        PreferenceCategory(title = "Wallpaper") {
             ListItem(
                headlineContent = { Text("Set Custom Wallpaper") },
                supportingContent = { Text("Pick an image from your gallery") },
                onClick = { wallpaperLauncher.launch(arrayOf("image/*")) }
             )
             
             if (state.wallpaperUri != null) {
                 ListItem(
                    headlineContent = { Text("Clear Wallpaper") },
                    onClick = { state.eventSink(ThemeSettingsEvents.SetWallpaper(null)) }
                 )
                 
                 PreferenceSwitch(
                    title = "Dim Wallpaper",
                    isChecked = state.wallpaperDim,
                    onCheckedChange = { state.eventSink(ThemeSettingsEvents.SetWallpaperDim(it)) }
                 )
             }
        }
    }
}

@Composable
fun PreferenceCategory(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
        )
        content()
    }
}

@Composable
private fun ThemeModeSection(
    theme: Theme,
    onThemeSelected: (Theme) -> Unit
) {
    // TODO: Use a proper selection UI like in AdvancedSettings
    // For now, simple list items
    ListItem(
        headlineContent = { Text(stringResource(CommonStrings.common_system)) },
        leadingContent = ListItemContent.RadioButton(selected = theme == Theme.System),
        onClick = { onThemeSelected(Theme.System) }
    )
    ListItem(
        headlineContent = { Text("Light") },
        leadingContent = ListItemContent.RadioButton(selected = theme == Theme.Light),
        onClick = { onThemeSelected(Theme.Light) }
    )
    ListItem(
        headlineContent = { Text("Dark") },
        leadingContent = ListItemContent.RadioButton(selected = theme == Theme.Dark),
        onClick = { onThemeSelected(Theme.Dark) }
    )
}
