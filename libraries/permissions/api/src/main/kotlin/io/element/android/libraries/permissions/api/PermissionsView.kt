/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.permissions.api

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
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
    content: String? = null,
    icon: @Composable (() -> Unit)? = null,
) {
    if (state.showDialog.not()) return

    ConfirmationDialog(
        modifier = modifier,
        title = title,
        content = content ?: state.permission.toDialogContent(),
        submitText = stringResource(id = CommonStrings.action_open_settings),
        onSubmitClick = {
            state.eventSink.invoke(PermissionsEvents.OpenSystemSettingAndCloseDialog)
        },
        onDismiss = { state.eventSink.invoke(PermissionsEvents.CloseDialog) },
        icon = icon,
    )
}

@Composable
@ReadOnlyComposable
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
