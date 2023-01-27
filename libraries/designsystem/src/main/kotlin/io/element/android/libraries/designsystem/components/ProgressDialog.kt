/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.libraries.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.ElementTheme
import io.element.android.libraries.designsystem.theme.components.ElementCircularProgressIndicator

@Composable
fun ProgressDialog(
    modifier: Modifier = Modifier,
    text: String? = null,
    onDismiss: () -> Unit = {},
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .fillMaxWidth()
                .background(
                    color = ElementTheme.colors.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ElementCircularProgressIndicator(
                    modifier = Modifier.padding(16.dp),
                    color = ElementTheme.colors.onSurfaceVariant
                )
                if (!text.isNullOrBlank()) {
                    Text(
                        text = text,
                        color = ElementTheme.colors.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ProgressDialogLightPreview() = ElementPreviewLight { ContentToPreview() }

@Preview
@Composable
fun ProgressDialogDarkPreview() = ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    ProgressDialog(text = "test dialog content")
}
