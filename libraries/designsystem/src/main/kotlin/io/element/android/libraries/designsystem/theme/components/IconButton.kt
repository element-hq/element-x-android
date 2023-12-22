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
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup

// Figma designs: https://www.figma.com/file/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?type=design&node-id=1182%3A48861&mode=design&t=Shlcvznm1oUyqGC2-1

@Composable
fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(
        contentColor = LocalContentColor.current,
        disabledContentColor = ElementTheme.colors.iconDisabled,
    ),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
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
        CompositionLocalProvider(LocalContentColor provides ElementTheme.colors.iconPrimary) {
            Row {
                IconButton(onClick = {}) {
                    Icon(imageVector = CompoundIcons.Close, contentDescription = null)
                }
                IconButton(enabled = false, onClick = {}) {
                    Icon(imageVector = CompoundIcons.Close, contentDescription = null)
                }
            }
        }
        CompositionLocalProvider(LocalContentColor provides ElementTheme.colors.iconSecondary) {
            Row {
                IconButton(onClick = {}) {
                    Icon(imageVector = CompoundIcons.Close, contentDescription = null)
                }
                IconButton(enabled = false, onClick = {}) {
                    Icon(imageVector = CompoundIcons.Close, contentDescription = null)
                }
            }
        }
    }
}
