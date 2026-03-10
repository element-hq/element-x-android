/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
internal fun LocationServiceDisabledDialog(
    onContinue: () -> Unit,
    onDismiss: () -> Unit,
) {
    ConfirmationDialog(
        content = "Location services are disabled. Please enable them in your device settings to use this feature.",
        onSubmitClick = onContinue,
        onDismiss = onDismiss,
        submitText = stringResource(CommonStrings.action_continue),
        cancelText = stringResource(CommonStrings.action_cancel),
    )
}
