/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer.appsettings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.components.preferences.PreferencePage
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AppDeveloperSettingsPage(
    state: AppDeveloperSettingsState,
    onOpenShowkase: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(
        onBack = onBackClick,
    )
    PreferencePage(
        modifier = modifier,
        onBackClick = {
            onBackClick()
        },
        title = "Application developer options",
    ) {
        AppDeveloperSettingsView(
            state = state,
            onOpenShowkase = onOpenShowkase,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@PreviewsDayNight
@Composable
internal fun AppDeveloperSettingsPagePreview() = ElementPreview {
    AppDeveloperSettingsPage(
        state = anAppDeveloperSettingsState(),
        onOpenShowkase = {},
        onBackClick = {},
    )
}
