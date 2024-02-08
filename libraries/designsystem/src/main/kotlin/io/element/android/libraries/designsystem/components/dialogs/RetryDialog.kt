/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.designsystem.components.dialogs

import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
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
        onSubmitClicked = onRetry,
        cancelText = dismissText,
        onCancelClicked = onDismiss,
    )
}

object RetryDialogDefaults {
    val title: String @Composable get() = stringResource(id = CommonStrings.dialog_title_error)
    val retryText: String @Composable get() = stringResource(id = CommonStrings.action_retry)
    val dismissText: String @Composable get() = stringResource(id = CommonStrings.action_cancel)
}

@Preview(group = PreviewGroup.Dialogs)
@Composable
internal fun RetryDialogPreview() {
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
