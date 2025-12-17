/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.logout.impl.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.element.android.features.logout.impl.R
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.dialogs.RetryDialog
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun LogoutActionDialog(
    state: AsyncAction<Unit>,
    onConfirmClick: () -> Unit,
    onForceLogoutClick: () -> Unit,
    onDismissDialog: () -> Unit,
) {
    when (state) {
        AsyncAction.Uninitialized ->
            Unit
        is AsyncAction.Confirming ->
            LogoutConfirmationDialog(
                onSubmitClick = onConfirmClick,
                onDismiss = onDismissDialog
            )
        is AsyncAction.Loading ->
            ProgressDialog(text = stringResource(id = R.string.screen_signout_in_progress_dialog_content))
        is AsyncAction.Failure ->
            RetryDialog(
                title = stringResource(id = CommonStrings.dialog_title_error),
                content = stringResource(id = CommonStrings.error_unknown),
                retryText = stringResource(id = CommonStrings.action_signout_anyway),
                onRetry = onForceLogoutClick,
                onDismiss = onDismissDialog,
            )
        is AsyncAction.Success -> Unit
    }
}
