/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun LocationConstraintsDialog(
    state: LocationConstraintsDialogState,
    appName: String,
    onRequestPermissions: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    when (state) {
        LocationConstraintsDialogState.None -> Unit
        LocationConstraintsDialogState.PermissionRationale -> ConfirmationDialog(
            content = stringResource(CommonStrings.error_missing_location_rationale_android, appName),
            onSubmitClick = onRequestPermissions,
            onDismiss = onDismiss,
            submitText = stringResource(CommonStrings.action_continue),
        )
        LocationConstraintsDialogState.PermissionDenied -> ConfirmationDialog(
            content = stringResource(CommonStrings.error_missing_location_auth_android, appName),
            onSubmitClick = onOpenAppSettings,
            onDismiss = onDismiss,
            submitText = stringResource(CommonStrings.action_continue),
        )
        LocationConstraintsDialogState.LocationServiceDisabled -> ConfirmationDialog(
            content = stringResource(CommonStrings.error_location_service_disabled_android),
            onSubmitClick = onOpenLocationSettings,
            onDismiss = onDismiss,
            submitText = stringResource(CommonStrings.action_continue),
        )
    }
}

@Immutable
sealed interface LocationConstraintsDialogState {
    data object None : LocationConstraintsDialogState
    data object PermissionRationale : LocationConstraintsDialogState
    data object PermissionDenied : LocationConstraintsDialogState
    data object LocationServiceDisabled : LocationConstraintsDialogState
}
