/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.dialogs

import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.DialogPreview
import io.element.android.libraries.designsystem.theme.components.SimpleAlertDialogContent
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetryDialog(
    content: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = RetryDialogDefaults.title,
    retryText: String = RetryDialogDefaults.retryText,
    dismissText: String = RetryDialogDefaults.dismissText,
) {
    BasicAlertDialog(modifier = modifier, onDismissRequest = onDismiss) {
        RetryDialogContent(
            title = title,
            content = content,
            retryText = retryText,
            dismissText = dismissText,
            onRetry = onRetry,
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun RetryDialogContent(
    content: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    title: String = RetryDialogDefaults.title,
    retryText: String = RetryDialogDefaults.retryText,
    dismissText: String = RetryDialogDefaults.dismissText,
) {
    SimpleAlertDialogContent(
        title = title,
        content = content,
        submitText = retryText,
        onSubmitClick = onRetry,
        cancelText = dismissText,
        onCancelClick = onDismiss,
    )
}

object RetryDialogDefaults {
    val title: String @Composable get() = stringResource(id = CommonStrings.dialog_title_error)
    val retryText: String @Composable get() = stringResource(id = CommonStrings.action_retry)
    val dismissText: String @Composable get() = stringResource(id = CommonStrings.action_cancel)
}

@Preview(group = PreviewGroup.Dialogs)
@Composable
internal fun RetryDialogContentPreview() {
    ElementThemedPreview(showBackground = false) {
        DialogPreview {
            RetryDialogContent(
                content = "Content",
                onRetry = {},
                onDismiss = {},
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun RetryDialogPreview() = ElementPreview {
    RetryDialog(
        content = "Content",
        onRetry = {},
        onDismiss = {},
    )
}
