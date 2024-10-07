/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.logout.impl.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.element.android.features.logout.impl.R
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun LogoutConfirmationDialog(
    onSubmitClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    ConfirmationDialog(
        title = stringResource(id = CommonStrings.action_signout),
        content = stringResource(id = R.string.screen_signout_confirmation_dialog_content),
        submitText = stringResource(id = CommonStrings.action_signout),
        onSubmitClick = onSubmitClick,
        onDismiss = onDismiss,
    )
}
