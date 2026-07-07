/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.localnetwork

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun LocalNetworkPermissionDialogView(
    dialog: LocalNetworkPermissionDialog,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (dialog) {
        LocalNetworkPermissionDialog.None -> Unit
        LocalNetworkPermissionDialog.Rationale -> ConfirmationDialog(
            title = stringResource(CommonStrings.screen_local_network_opt_in_title),
            content = stringResource(CommonStrings.screen_local_network_opt_in_subtitle),
            submitText = stringResource(CommonStrings.dialog_allow_access),
            cancelText = stringResource(CommonStrings.action_not_now),
            onSubmitClick = onSubmit,
            onDismiss = onDismiss,
            modifier = modifier,
        )
        LocalNetworkPermissionDialog.Settings -> ConfirmationDialog(
            title = stringResource(CommonStrings.screen_local_network_opt_in_title),
            content = stringResource(CommonStrings.screen_local_network_opt_in_subtitle),
            submitText = stringResource(CommonStrings.action_open_settings),
            cancelText = stringResource(CommonStrings.action_not_now),
            onSubmitClick = onSubmit,
            onDismiss = onDismiss,
            modifier = modifier,
        )
    }
}
