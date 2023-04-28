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

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.utils.BooleanProvider
import io.element.android.libraries.ui.strings.R as StringR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationDialog(
    content: String,
    onSubmitClicked: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    submitText: String = stringResource(id = StringR.string.action_ok),
    cancelText: String = stringResource(id = StringR.string.action_cancel),
    thirdButtonText: String? = null,
    emphasizeSubmitButton: Boolean = false,
    onCancelClicked: () -> Unit = onDismiss,
    onThirdButtonClicked: () -> Unit = {},
    shape: Shape = AlertDialogDefaults.shape,
    containerColor: Color = AlertDialogDefaults.containerColor,
    iconContentColor: Color = AlertDialogDefaults.iconContentColor,
    // According to the design team, `primary` should be used here instead of the default `onSurface`
    titleContentColor: Color = MaterialTheme.colorScheme.primary,
    textContentColor: Color = AlertDialogDefaults.textContentColor,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
) {
    AlertDialog(modifier = modifier, onDismissRequest = onDismiss) {
        ConfirmationDialogContent(
            title = title,
            content = content,
            submitText = submitText,
            cancelText = cancelText,
            thirdButtonText = thirdButtonText,
            onSubmitClicked = onSubmitClicked,
            onCancelClicked = onCancelClicked,
            onThirdButtonClicked = onThirdButtonClicked,
            shape = shape,
            containerColor = containerColor,
            iconContentColor = iconContentColor,
            titleContentColor = titleContentColor,
            textContentColor = textContentColor,
            tonalElevation = tonalElevation,
            emphasizeSubmitButton = emphasizeSubmitButton,
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
    modifier: Modifier = Modifier,
    title: String? = null,
    thirdButtonText: String? = null,
    onThirdButtonClicked: () -> Unit = {},
    emphasizeSubmitButton: Boolean = false,
    shape: Shape = AlertDialogDefaults.shape,
    containerColor: Color = AlertDialogDefaults.containerColor,
    iconContentColor: Color = AlertDialogDefaults.iconContentColor,
    titleContentColor: Color = AlertDialogDefaults.titleContentColor,
    textContentColor: Color = AlertDialogDefaults.textContentColor,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    icon: @Composable (() -> Unit)? = null,
) {
    SimpleAlertDialogContent(
        modifier = modifier,
        title = title,
        content = content,
        submitText = submitText,
        onSubmitClicked = onSubmitClicked,
        cancelText = cancelText,
        onCancelClicked = onCancelClicked,
        thirdButtonText = thirdButtonText,
        onThirdButtonClicked = onThirdButtonClicked,
        emphasizeSubmitButton = emphasizeSubmitButton,
        shape = shape,
        containerColor = containerColor,
        iconContentColor = iconContentColor,
        titleContentColor = titleContentColor,
        textContentColor = textContentColor,
        tonalElevation = tonalElevation,
        icon = icon,
    )
}

@Preview(group = PreviewGroup.Dialogs)
@Composable
internal fun ConfirmationDialogPreview(@PreviewParameter(BooleanProvider::class) emphasizeSubmitButton: Boolean) =
    ElementThemedPreview {
        DialogPreview {
            ConfirmationDialogContent(
                content = "Content",
                title = "Title",
                submitText = "OK",
                cancelText = "Cancel",
                thirdButtonText = "Disable",
                onSubmitClicked = {},
                onCancelClicked = {},
                emphasizeSubmitButton = emphasizeSubmitButton,
            )
        }
    }
