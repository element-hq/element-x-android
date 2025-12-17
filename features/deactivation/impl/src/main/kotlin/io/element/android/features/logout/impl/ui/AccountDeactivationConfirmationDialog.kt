/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.logout.impl.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.element.android.features.deactivation.impl.R
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun AccountDeactivationConfirmationDialog(
    onSubmitClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    ConfirmationDialog(
        title = stringResource(id = R.string.screen_deactivate_account_title),
        content = stringResource(R.string.screen_deactivate_account_confirmation_dialog_content),
        submitText = stringResource(id = CommonStrings.action_deactivate),
        onSubmitClick = onSubmitClick,
        onDismiss = onDismiss,
        destructiveSubmit = true,
    )
}
