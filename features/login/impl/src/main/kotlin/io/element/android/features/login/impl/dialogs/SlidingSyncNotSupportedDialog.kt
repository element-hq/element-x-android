/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.element.android.features.login.impl.R
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.LocalBuildMeta
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
internal fun SlidingSyncNotSupportedDialog(
    onLearnMoreClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ConfirmationDialog(
        modifier = modifier,
        onDismiss = onDismiss,
        submitText = stringResource(CommonStrings.action_learn_more),
        onSubmitClick = onLearnMoreClick,
        onCancelClick = onDismiss,
        title = stringResource(CommonStrings.dialog_title_error),
        content = stringResource(
            id = R.string.screen_change_server_error_no_sliding_sync_message,
            LocalBuildMeta.current.applicationName,
        ),
    )
}

@PreviewsDayNight
@Composable
internal fun SlidingSyncNotSupportedDialogPreview() = ElementPreview {
    SlidingSyncNotSupportedDialog(
        onLearnMoreClick = {},
        onDismiss = {},
    )
}
