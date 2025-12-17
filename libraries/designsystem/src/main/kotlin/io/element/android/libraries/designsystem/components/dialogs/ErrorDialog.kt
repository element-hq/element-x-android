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
import androidx.compose.ui.window.DialogProperties
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.DialogPreview
import io.element.android.libraries.designsystem.theme.components.SimpleAlertDialogContent
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorDialog(
    content: String,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = ErrorDialogDefaults.title,
    submitText: String = ErrorDialogDefaults.submitText,
    onDismiss: () -> Unit = onSubmit,
    canDismiss: Boolean = true,
) {
    BasicAlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = canDismiss, dismissOnBackPress = canDismiss)
    ) {
        ErrorDialogContent(
            title = title,
            content = content,
            submitText = submitText,
            onSubmitClick = onSubmit,
        )
    }
}

@Composable
private fun ErrorDialogContent(
    content: String,
    onSubmitClick: () -> Unit,
    title: String? = ErrorDialogDefaults.title,
    submitText: String = ErrorDialogDefaults.submitText,
) {
    SimpleAlertDialogContent(
        title = title,
        content = content,
        submitText = submitText,
        onSubmitClick = onSubmitClick,
    )
}

object ErrorDialogDefaults {
    val title: String @Composable get() = stringResource(id = CommonStrings.dialog_title_error)
    val submitText: String @Composable get() = stringResource(id = CommonStrings.action_ok)
}

@Preview(group = PreviewGroup.Dialogs)
@Composable
internal fun ErrorDialogContentPreview() {
    ElementThemedPreview(showBackground = false) {
        DialogPreview {
            ErrorDialogContent(
                content = "Content",
                onSubmitClick = {},
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun ErrorDialogPreview() = ElementPreview {
    ErrorDialog(
        content = "Content",
        onSubmit = {},
    )
}
