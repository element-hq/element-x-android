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

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun PermissionsView(
    state: PermissionsState,
    modifier: Modifier = Modifier,
    title: String = stringResource(id = CommonStrings.common_permission),
    icon: @Composable (() -> Unit)? = null,
) {
    if (state.showDialog.not()) return

    ConfirmationDialog(
        modifier = modifier,
        title = title,
        content = state.permission.toDialogContent(),
        submitText = stringResource(id = CommonStrings.action_open_settings),
        onSubmitClicked = {
            state.eventSink.invoke(PermissionsEvents.OpenSystemSettingAndCloseDialog)
        },
        onDismiss = { state.eventSink.invoke(PermissionsEvents.CloseDialog) },
        icon = icon,
    )
}

@Composable
private fun String.toDialogContent(): String {
    return when (this) {
        Manifest.permission.POST_NOTIFICATIONS -> stringResource(id = R.string.dialog_permission_notification)
        Manifest.permission.CAMERA -> stringResource(id = R.string.dialog_permission_camera)
        Manifest.permission.RECORD_AUDIO -> stringResource(id = R.string.dialog_permission_microphone)
        else -> stringResource(id = R.string.dialog_permission_generic)
    }
}

@PreviewsDayNight
@Composable
internal fun PermissionsViewPreview(@PreviewParameter(PermissionsStateProvider::class) state: PermissionsState) = ElementPreview {
    PermissionsView(
        state = state,
    )
}
