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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.R as StringR

@Composable
fun ErrorDialog(
    content: String,
    modifier: Modifier = Modifier,
    title: String = ErrorDialogDefaults.title,
    submitText: String = ErrorDialogDefaults.submitText,
    dismissText: String? = null,
    onDismiss: () -> Unit = {},
    onSubmit: () -> Unit = onDismiss,
    shape: Shape = AlertDialogDefaults.shape,
    containerColor: Color = AlertDialogDefaults.containerColor,
    iconContentColor: Color = AlertDialogDefaults.iconContentColor,
    titleContentColor: Color = AlertDialogDefaults.titleContentColor,
    textContentColor: Color = AlertDialogDefaults.textContentColor,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(title)
        },
        text = {
            Text(content)
        },
        confirmButton = {
            TextButton(onClick = onSubmit) {
                Text(submitText)
            }
        },
        dismissButton = dismissText?.let {
            {
                TextButton(onClick = onDismiss) {
                    Text(it)
                }
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

object ErrorDialogDefaults {
    val title: String @Composable get() = stringResource(id = StringR.string.dialog_title_error)
    val submitText: String @Composable get() = stringResource(id = StringR.string.action_ok)
}

@Preview
@Composable
internal fun ErrorDialogLightPreview() = ElementPreviewLight { ContentToPreview() }

@Preview
@Composable
internal fun ErrorDialogDarkPreview() = ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    ErrorDialog(
        content = "Content",
    )
}
