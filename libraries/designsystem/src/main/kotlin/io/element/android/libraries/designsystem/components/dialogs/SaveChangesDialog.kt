/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun SaveChangesDialog(
    onSubmitClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(CommonStrings.dialog_unsaved_changes_title),
    content: String = stringResource(CommonStrings.dialog_unsaved_changes_description_android),
) = ConfirmationDialog(
    modifier = modifier,
    title = title,
    content = content,
    onSubmitClick = onSubmitClick,
    onDismiss = onDismiss,
)

@PreviewsDayNight
@Composable
internal fun SaveChangeDialogPreview() = ElementPreview {
    SaveChangesDialog(
        onSubmitClick = {},
        onDismiss = {}
    )
}
