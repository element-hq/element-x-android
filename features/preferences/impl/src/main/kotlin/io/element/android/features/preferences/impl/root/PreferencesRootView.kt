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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.logout.api.LogoutPreferenceView
import io.element.android.features.preferences.impl.user.UserPreferences
import io.element.android.features.rageshake.api.preferences.RageshakePreferencesView
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.designsystem.components.preferences.PreferenceView
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.matrix.ui.components.MatrixUserProvider
import io.element.android.libraries.matrix.ui.model.MatrixUser
import io.element.android.libraries.ui.strings.R as StringR

@Composable
fun PreferencesRootView(
    state: PreferencesRootState,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
    onOpenRageShake: () -> Unit = {},
) {
    // TODO Hierarchy!
    // Include pref from other modules
    PreferenceView(
        modifier = modifier,
        onBackPressed = onBackPressed,
        title = stringResource(id = StringR.string.settings)
    ) {
        UserPreferences(state.myUser)
        RageshakePreferencesView(
            state = state.rageshakeState,
            onOpenRageshake = onOpenRageShake,
        )
        LogoutPreferenceView(
            state = state.logoutState,
        )
    }
}

@Preview
@Composable
fun PreferencesRootViewLightPreview(@PreviewParameter(MatrixUserProvider::class) matrixUser: MatrixUser) =
    ElementPreviewLight { ContentToPreview(matrixUser) }

@Preview
@Composable
fun PreferencesRootViewDarkPreview(@PreviewParameter(MatrixUserProvider::class) matrixUser: MatrixUser) =
    ElementPreviewDark { ContentToPreview(matrixUser) }

@Composable
private fun ContentToPreview(matrixUser: MatrixUser) {
    PreferencesRootView(aPreferencesRootState().copy(myUser = Async.Success(matrixUser)))
}
