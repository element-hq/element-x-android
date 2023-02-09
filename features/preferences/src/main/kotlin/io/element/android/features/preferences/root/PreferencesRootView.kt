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

package io.element.android.features.preferences.root

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.element.android.features.logout.LogoutPreferenceState
import io.element.android.features.logout.LogoutPreferenceView
import io.element.android.features.preferences.user.UserPreferences
import io.element.android.features.rageshake.preferences.RageshakePreferencesState
import io.element.android.features.rageshake.preferences.RageshakePreferencesView
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.designsystem.components.preferences.PreferenceView
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewDefaults
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

@PreviewDefaults
@Composable
fun PreferencesRootViewPreview() = ElementPreview {
    val state = PreferencesRootState(
        logoutState = LogoutPreferenceState(),
        rageshakeState = RageshakePreferencesState(),
        myUser = Async.Uninitialized
    )
    PreferencesRootView(state)
}
