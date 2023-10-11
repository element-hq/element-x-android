/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.features.logout.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.components.preferences.PreferenceText
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.utils.CommonDrawables

@Composable
fun LogoutPreferenceView(
    state: LogoutPreferenceState,
    onSuccessLogout: (logoutUrlResult: String?) -> Unit
) {
    val eventSink = state.eventSink
    if (state.logoutAction is Async.Success) {
        LaunchedEffect(state.logoutAction) {
            onSuccessLogout(state.logoutAction.data)
        }
        return
    }
    val openDialog = remember { mutableStateOf(false) }

    LogoutPreferenceContent(
        onClick = {
            openDialog.value = true
        }
    )

    // Log out confirmation dialog
    if (openDialog.value) {
        ConfirmationDialog(
            title = stringResource(id = R.string.screen_signout_confirmation_dialog_title),
            content = stringResource(id = R.string.screen_signout_confirmation_dialog_content),
            submitText = stringResource(id = R.string.screen_signout_confirmation_dialog_submit),
            onCancelClicked = {
                openDialog.value = false
            },
            onSubmitClicked = {
                openDialog.value = false
                eventSink(LogoutPreferenceEvents.Logout)
            },
            onDismiss = {
                openDialog.value = false
            }
        )
    }

    if (state.logoutAction is Async.Loading) {
        ProgressDialog(text = stringResource(id = R.string.screen_signout_in_progress_dialog_content))
    }
}

@Composable
private fun LogoutPreferenceContent(
    onClick: () -> Unit = {},
) {
    PreferenceText(
        title = stringResource(id = R.string.screen_signout_preference_item),
        iconResourceId = CommonDrawables.ic_compound_leave,
        onClick = onClick
    )
}

@PreviewsDayNight
@Composable
internal fun LogoutPreferenceViewPreview() = ElementPreview {
    LogoutPreferenceView(
        aLogoutPreferenceState(),
        onSuccessLogout = {}
    )
}
