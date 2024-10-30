/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup

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
        modifier = modifier.minimumInteractiveComponentSize(),
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
    )
}

@Composable
internal fun compoundRadioButtonColors(): RadioButtonColors {
    return RadioButtonDefaults.colors(
        unselectedColor = ElementTheme.colors.borderInteractivePrimary,
        selectedColor = ElementTheme.colors.bgAccentRest,
        disabledUnselectedColor = ElementTheme.colors.borderDisabled,
        disabledSelectedColor = ElementTheme.colors.iconDisabled,
    )
}

@Preview(group = PreviewGroup.Toggles)
@Composable
internal fun RadioButtonPreview() = ElementThemedPreview(vertical = false) {
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
