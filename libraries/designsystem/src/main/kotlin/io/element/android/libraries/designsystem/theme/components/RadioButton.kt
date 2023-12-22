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
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.compound.theme.ElementTheme

// Designs in https://www.figma.com/file/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?type=design&node-id=425%3A24202&mode=design&t=qb99xBP5mwwCtGkN-1

@Composable
fun RadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: RadioButtonColors = compoundRadioButtonColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    androidx.compose.material3.RadioButton(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
    )
}

@Composable
internal fun compoundRadioButtonColors(): RadioButtonColors {
    return RadioButtonDefaults.colors(
        unselectedColor = ElementTheme.colors.borderInteractivePrimary,
        disabledUnselectedColor = ElementTheme.colors.borderDisabled,
        disabledSelectedColor = ElementTheme.colors.iconDisabled,
    )
}

@Preview(group = PreviewGroup.Toggles)
@Composable
internal fun RadioButtonPreview() = ElementThemedPreview(vertical = false) { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    var checked by remember { mutableStateOf(false) }
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            RadioButton(selected = checked, enabled = true, onClick = { checked = !checked })
            RadioButton(selected = checked, enabled = false, onClick = { checked = !checked })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            RadioButton(selected = !checked, enabled = true, onClick = { checked = !checked })
            RadioButton(selected = !checked, enabled = false, onClick = { checked = !checked })
        }
    }
}
