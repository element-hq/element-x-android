/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup

@Composable
fun IconToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconToggleButtonColors = IconButtonDefaults.iconToggleButtonColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    androidx.compose.material3.IconToggleButton(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
        content = content,
    )
}

@Preview(group = PreviewGroup.Toggles)
@Composable
internal fun IconToggleButtonPreview() = ElementThemedPreview(vertical = false) {
    var checked by remember { mutableStateOf(false) }
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val icon: @Composable () -> Unit = {
                Icon(
                    imageVector = if (checked) CompoundIcons.CheckCircleSolid() else CompoundIcons.Circle(),
                    contentDescription = null
                )
            }
            IconToggleButton(checked = checked, enabled = true, onCheckedChange = { checked = !checked }, content = icon)
            IconToggleButton(checked = checked, enabled = false, onCheckedChange = { checked = !checked }, content = icon)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val icon: @Composable () -> Unit = {
                Icon(
                    imageVector = if (!checked) CompoundIcons.CheckCircleSolid() else CompoundIcons.Circle(),
                    contentDescription = null
                )
            }
            IconToggleButton(checked = !checked, enabled = true, onCheckedChange = { checked = !checked }, content = icon)
            IconToggleButton(checked = !checked, enabled = false, onCheckedChange = { checked = !checked }, content = icon)
        }
    }
}
