/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.preferences.impl.root

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.InsertChart
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.logout.api.LogoutPreferenceView
import io.element.android.features.preferences.impl.user.UserPreferences
import io.element.android.libraries.designsystem.components.preferences.PreferenceText
import io.element.android.libraries.designsystem.components.preferences.PreferenceView
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.preview.LargeHeightPreview
import io.element.android.libraries.designsystem.theme.components.Divider
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.MatrixUserProvider
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun PreferencesRootView(
    state: PreferencesRootState,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit,
    onOpenAnalytics: () -> Unit,
    onOpenRageShake: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenDeveloperSettings: () -> Unit,
) {
    // Include pref from other modules
    PreferenceView(
        modifier = modifier,
        onBackPressed = onBackPressed,
        title = stringResource(id = CommonStrings.common_settings)
    ) {
        UserPreferences(state.myUser)
        // TODO Verification and eventually divider
        PreferenceText(
            title = stringResource(id = CommonStrings.common_analytics),
            icon = Icons.Outlined.InsertChart,
            onClick = onOpenAnalytics,
        )
        PreferenceText(
            title = stringResource(id = CommonStrings.action_report_bug),
            icon = Icons.Outlined.BugReport,
            onClick = onOpenRageShake
        )
        PreferenceText(
            title = stringResource(id = CommonStrings.common_about),
            icon = Icons.Filled.Help,
            onClick = onOpenAbout,
        )
        if (state.showDeveloperSettings) {
            DeveloperPreferencesView(onOpenDeveloperSettings)
        }
        Divider()
        LogoutPreferenceView(
            state = state.logoutState,
        )
    }
}

@Composable
fun DeveloperPreferencesView(onOpenDeveloperSettings: () -> Unit) {
    PreferenceText(
        title = stringResource(id = CommonStrings.common_developer_options),
        icon = Icons.Default.DeveloperMode,
        onClick = onOpenDeveloperSettings
    )
}

@LargeHeightPreview
@Composable
fun PreferencesRootViewLightPreview(@PreviewParameter(MatrixUserProvider::class) matrixUser: MatrixUser) =
    ElementPreviewLight { ContentToPreview(matrixUser) }

@LargeHeightPreview
@Composable
fun PreferencesRootViewDarkPreview(@PreviewParameter(MatrixUserProvider::class) matrixUser: MatrixUser) =
    ElementPreviewDark { ContentToPreview(matrixUser) }

@Composable
private fun ContentToPreview(matrixUser: MatrixUser) {
    PreferencesRootView(
        state = aPreferencesRootState().copy(myUser = matrixUser),
        onBackPressed = {},
        onOpenAnalytics = {},
        onOpenRageShake = {},
        onOpenDeveloperSettings = {},
        onOpenAbout = {},
    )
}
