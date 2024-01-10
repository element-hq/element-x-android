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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import androidx.compose.material3.Switch as Material3Switch

// Designs in https://www.figma.com/file/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?type=design&node-id=425%3A24203&mode=design&t=qb99xBP5mwwCtGkN-1

@Composable
fun Switch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: SwitchColors = compoundSwitchColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    thumbContent: (@Composable () -> Unit)? = null,
) {
    Material3Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
        thumbContent = thumbContent
    )
}

@Composable
internal fun compoundSwitchColors() = SwitchDefaults.colors(
    uncheckedThumbColor = ElementTheme.colors.bgActionPrimaryRest,
    uncheckedTrackColor = Color.Transparent,
    disabledUncheckedBorderColor = ElementTheme.colors.borderDisabled,
    disabledUncheckedThumbColor = ElementTheme.colors.iconDisabled,
    disabledCheckedTrackColor = ElementTheme.colors.iconDisabled,
    disabledCheckedBorderColor = ElementTheme.colors.iconDisabled,
)

@Preview(group = PreviewGroup.Toggles)
@Composable
internal fun SwitchPreview() {
    var checked by remember { mutableStateOf(false) }
    ElementThemedPreview {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Switch(checked = checked, onCheckedChange = { checked = !checked })
                Switch(enabled = false, checked = checked, onCheckedChange = { checked = !checked })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Switch(checked = !checked, onCheckedChange = { checked = !checked })
                Switch(enabled = false, checked = !checked, onCheckedChange = { checked = !checked })
            }
        }
    }
}
