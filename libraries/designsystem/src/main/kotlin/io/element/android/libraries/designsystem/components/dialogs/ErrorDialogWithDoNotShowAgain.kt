/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Checkbox
import io.element.android.libraries.designsystem.theme.components.SimpleAlertDialogContent
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorDialogWithDoNotShowAgain(
    content: String,
    onDismiss: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    title: String = ErrorDialogDefaults.title,
    submitText: String = ErrorDialogDefaults.submitText,
    cancelText: String? = null,
    onCancel: () -> Unit = {},
) {
    var doNotShowAgain by remember { mutableStateOf(false) }
    BasicAlertDialog(
        modifier = modifier,
        onDismissRequest = { onDismiss(doNotShowAgain) }
    ) {
        SimpleAlertDialogContent(
            title = title,
            submitText = submitText,
            cancelText = cancelText,
            onSubmitClick = { onDismiss(doNotShowAgain) },
            onCancelClick = onCancel,
        ) {
            Column {
                Text(
                    text = content,
                    style = ElementTheme.materialTypography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = doNotShowAgain, onCheckedChange = { doNotShowAgain = it })
                    Text(
                        text = stringResource(id = CommonStrings.common_do_not_show_this_again),
                        style = ElementTheme.materialTypography.bodyMedium,
                    )
                }
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun ErrorDialogWithDoNotShowAgainPreview() = ElementPreview {
    ErrorDialogWithDoNotShowAgain(
        content = "Content",
        onDismiss = {},
    )
}
