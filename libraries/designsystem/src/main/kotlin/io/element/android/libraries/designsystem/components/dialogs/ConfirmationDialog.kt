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
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.DialogPreview
import io.element.android.libraries.designsystem.theme.components.SimpleAlertDialogContent
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationDialog(
    content: String,
    onSubmitClicked: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    submitText: String = stringResource(id = CommonStrings.action_ok),
    cancelText: String = stringResource(id = CommonStrings.action_cancel),
    destructiveSubmit: Boolean = false,
    thirdButtonText: String? = null,
    onCancelClicked: () -> Unit = onDismiss,
    onThirdButtonClicked: () -> Unit = {},
) {
    BasicAlertDialog(modifier = modifier, onDismissRequest = onDismiss) {
        ConfirmationDialogContent(
            title = title,
            content = content,
            submitText = submitText,
            cancelText = cancelText,
            thirdButtonText = thirdButtonText,
            destructiveSubmit = destructiveSubmit,
            onSubmitClicked = onSubmitClicked,
            onCancelClicked = onCancelClicked,
            onThirdButtonClicked = onThirdButtonClicked,
        )
    }
}

@Composable
private fun ConfirmationDialogContent(
    content: String,
    submitText: String,
    cancelText: String,
    onSubmitClicked: () -> Unit,
    onCancelClicked: () -> Unit,
    title: String? = null,
    thirdButtonText: String? = null,
    onThirdButtonClicked: () -> Unit = {},
    destructiveSubmit: Boolean = false,
    icon: @Composable (() -> Unit)? = null,
) {
    SimpleAlertDialogContent(
        title = title,
        content = content,
        submitText = submitText,
        onSubmitClicked = onSubmitClicked,
        cancelText = cancelText,
        onCancelClicked = onCancelClicked,
        thirdButtonText = thirdButtonText,
        onThirdButtonClicked = onThirdButtonClicked,
        destructiveSubmit = destructiveSubmit,
        icon = icon,
    )
}

@Preview(group = PreviewGroup.Dialogs)
@Composable
internal fun ConfirmationDialogContentPreview() =
    ElementThemedPreview(showBackground = false) {
        DialogPreview {
            ConfirmationDialogContent(
                content = "Content",
                title = "Title",
                submitText = "OK",
                cancelText = "Cancel",
                thirdButtonText = "Disable",
                onSubmitClicked = {},
                onCancelClicked = {},
                onThirdButtonClicked = {},
            )
        }
    }

@PreviewsDayNight
@Composable
internal fun ConfirmationDialogPreview() = ElementPreview {
    ConfirmationDialog(
        content = "Content",
        title = "Title",
        submitText = "OK",
        cancelText = "Cancel",
        thirdButtonText = "Disable",
        onSubmitClicked = {},
        onDismiss = {}
    )
}
