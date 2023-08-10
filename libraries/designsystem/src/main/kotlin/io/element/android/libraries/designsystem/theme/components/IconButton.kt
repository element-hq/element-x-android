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

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.theme.ElementTheme

@Composable
fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    val colors = IconButtonDefaults.iconButtonColors(
        contentColor = if (isPrimary) ElementTheme.colors.iconPrimary else ElementTheme.colors.iconSecondary,
        disabledContentColor = ElementTheme.colors.iconDisabled,
    )
    androidx.compose.material3.IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
        content = content,
    )
}

@Preview(group = PreviewGroup.Buttons)
@Composable
internal fun IconButtonPreview() =
    ElementThemedPreview { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    Column {
        Row {
            IconButton(onClick = {}) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = "")
            }
            IconButton(enabled = false, onClick = {}) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = "")
            }
        }
        Row {
            IconButton(onClick = {}, isPrimary = false) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = "")
            }
            IconButton(isPrimary = false, enabled = false, onClick = {}) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = "")
            }
        }
    }
}
