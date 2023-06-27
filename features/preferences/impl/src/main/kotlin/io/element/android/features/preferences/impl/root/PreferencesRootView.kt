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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.logout.api.LogoutPreferenceView
import io.element.android.features.preferences.impl.user.UserPreferences
import io.element.android.features.analytics.api.preferences.AnalyticsPreferencesView
import io.element.android.features.rageshake.api.preferences.RageshakePreferencesView
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.designsystem.components.preferences.PreferenceCategory
import io.element.android.libraries.designsystem.components.preferences.PreferenceText
import io.element.android.libraries.designsystem.components.preferences.PreferenceView
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.preview.LargeHeightPreview
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.MatrixUserProvider
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun PreferencesRootView(
    state: PreferencesRootState,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
    onOpenRageShake: () -> Unit = {},
    onOpenDeveloperSettings: () -> Unit = {},
) {
    // TODO Hierarchy!
    // Include pref from other modules
    PreferenceView(
        modifier = modifier,
        onBackPressed = onBackPressed,
        title = stringResource(id = CommonStrings.common_settings)
    ) {
        UserPreferences(state.myUser)
        AnalyticsPreferencesView(
            state = state.analyticsState,
        )
        RageshakePreferencesView(
            state = state.rageshakeState,
            onOpenRageshake = onOpenRageShake,
        )
        LogoutPreferenceView(
            state = state.logoutState,
        )
        if (state.showDeveloperSettings) {
            DeveloperPreferencesView(onOpenDeveloperSettings)
        }
    }
}

@Composable
fun DeveloperPreferencesView(onOpenDeveloperSettings: () -> Unit) {
    PreferenceCategory(title = stringResource(id = CommonStrings.common_developer_options)) {
        PreferenceText(
            title = stringResource(id = CommonStrings.common_developer_options),
            icon = Icons.Default.DeveloperMode,
            onClick = onOpenDeveloperSettings
        )
    }
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
    PreferencesRootView(aPreferencesRootState().copy(myUser = Async.Success(matrixUser)))
}
