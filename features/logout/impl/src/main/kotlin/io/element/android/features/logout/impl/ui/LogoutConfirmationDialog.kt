/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

// Modified by Feral: "Log out" wording + recovery-key reminder (Feral-owned strings).

package io.element.android.features.logout.impl.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun LogoutConfirmationDialog(
    onSubmitClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    ConfirmationDialog(
        title = stringResource(id = CommonStrings.feral_logout_action),
        content = stringResource(id = CommonStrings.feral_logout_confirmation_content),
        submitText = stringResource(id = CommonStrings.feral_logout_action),
        onSubmitClick = onSubmitClick,
        onDismiss = onDismiss,
    )
}
