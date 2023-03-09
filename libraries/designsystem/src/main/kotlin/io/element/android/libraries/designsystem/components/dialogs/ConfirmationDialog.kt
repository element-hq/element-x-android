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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import io.element.android.libraries.designsystem.ElementTextStyles
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.R as StringR

@Composable
fun ConfirmationDialog(
    title: String,
    content: String,
    onSubmitClicked: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    submitText: String = stringResource(id = StringR.string.ok),
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
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(text = title)
        },
        text = {
            Text(content)
        },
        dismissButton = {
            if (thirdButtonText != null) {
                    // If there is a 3rd item it should be at the end of the dialog
                    // Having this 3rd action is discouraged, see https://m3.material.io/components/dialogs/guidelines#e13b68f5-e367-4275-ad6f-c552ee8e358f
                    TextButton(onClick = onThirdButtonClicked) {
                        Text(thirdButtonText)
                    }
                }
            TextButton(onClick = onCancelClicked) {
                Text(cancelText)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSubmitClicked()
                },
            ) {
                Text(
                    submitText,
                    style = if (emphasizeSubmitButton) {
                        ElementTextStyles.Bold.subheadline
                    } else {
                        MaterialTheme.typography.labelLarge
                    }
                )
            }
        },
        shape = shape,
        containerColor = containerColor,
        iconContentColor = iconContentColor,
        titleContentColor = titleContentColor,
        textContentColor = textContentColor,
        tonalElevation = tonalElevation,
    )
}

@Preview
@Composable
internal fun ConfirmationDialogLightPreview() = ElementPreviewLight { ContentToPreview() }

@Preview
@Composable
internal fun ConfirmationDialogDarkPreview() = ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    ConfirmationDialog(
        title = "Title",
        content = "Content",
        thirdButtonText = "Disable",
        onSubmitClicked = {},
        onDismiss = {},
        emphasizeSubmitButton = true,
    )
}
