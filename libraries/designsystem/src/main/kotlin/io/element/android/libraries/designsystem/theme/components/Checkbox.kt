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
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup

// Designs in https://www.figma.com/file/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?type=design&mode=design&t=qb99xBP5mwwCtGkN-1

@Composable
fun Checkbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    hasError: Boolean = false,
    indeterminate: Boolean = false,
    colors: CheckboxColors = if (hasError) compoundErrorCheckBoxColors() else compoundCheckBoxColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    var indeterminateState by remember { mutableStateOf(indeterminate) }
    androidx.compose.material3.TriStateCheckbox(
        state = if (!checked && indeterminateState) ToggleableState.Indeterminate else ToggleableState(checked),
        onClick = onCheckedChange?.let {
            {
                indeterminateState = false
                onCheckedChange(!checked)
            }
        },
        modifier = modifier.minimumInteractiveComponentSize(),
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
    )
}

@Composable
private fun compoundCheckBoxColors(): CheckboxColors {
    return CheckboxDefaults.colors(
        checkedColor = ElementTheme.colors.bgAccentRest,
        uncheckedColor = ElementTheme.colors.borderInteractivePrimary,
        checkmarkColor = ElementTheme.materialColors.onPrimary,
        disabledUncheckedColor = ElementTheme.colors.borderDisabled,
        disabledCheckedColor = ElementTheme.colors.iconDisabled,
        disabledIndeterminateColor = ElementTheme.colors.iconDisabled,
    )
}

@Composable
private fun compoundErrorCheckBoxColors(): CheckboxColors {
    return CheckboxDefaults.colors(
        checkedColor = ElementTheme.materialColors.error,
        uncheckedColor = ElementTheme.materialColors.error,
        checkmarkColor = ElementTheme.materialColors.onPrimary,
        disabledUncheckedColor = ElementTheme.colors.borderDisabled,
        disabledCheckedColor = ElementTheme.colors.iconDisabled,
        disabledIndeterminateColor = ElementTheme.colors.iconDisabled,
    )
}

@Preview(group = PreviewGroup.Toggles)
@Composable
internal fun CheckboxesPreview() = ElementThemedPreview(vertical = false) {
    Column {
        // Unchecked
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Checkbox(onCheckedChange = {}, enabled = true, checked = false)
            Checkbox(onCheckedChange = {}, enabled = false, checked = false)
        }
        // Checked
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Checkbox(onCheckedChange = {}, enabled = true, checked = true)
            Checkbox(onCheckedChange = {}, enabled = false, checked = true)
        }
        // Indeterminate
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Checkbox(onCheckedChange = {}, enabled = true, checked = false, indeterminate = true)
            Checkbox(onCheckedChange = {}, enabled = false, checked = false, indeterminate = true)
        }
        // Error
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Checkbox(hasError = true, onCheckedChange = {}, checked = false)
            Checkbox(hasError = true, onCheckedChange = {}, enabled = false, checked = false)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Checkbox(hasError = true, onCheckedChange = {}, enabled = true, checked = true)
            Checkbox(hasError = true, onCheckedChange = {}, enabled = false, checked = true)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Checkbox(onCheckedChange = {}, enabled = true, checked = false, indeterminate = true, hasError = true)
            Checkbox(onCheckedChange = {}, enabled = false, checked = false, indeterminate = true, hasError = true)
        }
    }
}
