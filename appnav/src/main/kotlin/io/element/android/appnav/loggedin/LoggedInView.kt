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

package io.element.android.appnav.loggedin

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.libraries.androidutils.system.openAppSettingsPage
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.permissions.api.PermissionsView

@Composable
fun LoggedInView(
    state: LoggedInState,
    modifier: Modifier = Modifier
) {
    val activity = LocalContext.current as? Activity

    PermissionsView(
        state = state.permissionsState,
        modifier = modifier,
        openSystemSettings = {
            activity?.let { openAppSettingsPage(it, "") }
        }
    )
}

@Preview
@Composable
fun LoggedInViewLightPreview(@PreviewParameter(LoggedInStateProvider::class) state: LoggedInState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
fun LoggedInViewDarkPreview(@PreviewParameter(LoggedInStateProvider::class) state: LoggedInState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: LoggedInState) {
    LoggedInView(
        state = state
    )
}
