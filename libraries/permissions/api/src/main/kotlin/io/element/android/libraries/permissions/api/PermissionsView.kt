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

package io.element.android.libraries.permissions.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight

@Composable
fun PermissionsView(
    state: PermissionsState,
    modifier: Modifier = Modifier,
    openSystemSettings: () -> Unit = {},
) {
    if (state.showDialog.not()) return

    if (state.permissionGranted) {
        // Notification Granted, nothing to do
    } else if (state.permissionAlreadyDenied) {
        // In this case, tell the user to go to the settings
        ConfirmationDialog(
            modifier = modifier,
            title = "System",
            content = "In order to let the application display notification, please grant the permission to the system settings",
            submitText = "Open settings",
            onSubmitClicked = {
                state.eventSink.invoke(PermissionsEvents.OpenSystemSettings)
                openSystemSettings()
            },
            onDismiss = { state.eventSink.invoke(PermissionsEvents.CloseDialog) },
        )
    } else {
        val textToShow = if (state.shouldShowRationale) {
            // TODO Move to state
            // If the user has denied the permission but the rationale can be shown,
            // then gently explain why the app requires this permission
            // permissions_rationale_msg_notification
            "To be able to receive notifications, please grant the permission. Else you will not be able to be alerted if you've got new messages."
        } else {
            // TODO Move to state
            // If it's the first time the user lands on this feature, or the user
            // doesn't want to be asked again for this permission, explain that the
            // permission is required
            "To be able to receive notifications, please grant the permission."
        }
        ConfirmationDialog(
            modifier = modifier,
            title = "Notifications",
            content = textToShow,
            submitText = "Request permission",
            onSubmitClicked = {
                state.eventSink.invoke(PermissionsEvents.OpenSystemDialog)
            },
            onCancelClicked = {
                state.eventSink.invoke(PermissionsEvents.CloseDialog)
            },
            onDismiss = {}
        )
    }
}

@Preview
@Composable
fun PermissionsViewLightPreview(@PreviewParameter(PermissionsViewStateProvider::class) state: PermissionsState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
fun PermissionsViewDarkPreview(@PreviewParameter(PermissionsViewStateProvider::class) state: PermissionsState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: PermissionsState) {
    PermissionsView(
        state = state,
    )
}
