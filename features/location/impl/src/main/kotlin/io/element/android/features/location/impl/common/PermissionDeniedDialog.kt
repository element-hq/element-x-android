/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
internal fun PermissionDeniedDialog(
    onContinue: () -> Unit,
    onDismiss: () -> Unit,
    appName: String,
) {
    ConfirmationDialog(
        content = stringResource(CommonStrings.error_missing_location_auth_android, appName),
        onSubmitClick = onContinue,
        onDismiss = onDismiss,
        submitText = stringResource(CommonStrings.action_continue),
        cancelText = stringResource(CommonStrings.action_cancel),
    )
}
