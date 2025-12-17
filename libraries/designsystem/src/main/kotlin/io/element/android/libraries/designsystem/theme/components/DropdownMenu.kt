/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import io.element.android.compound.theme.ElementTheme

// Figma designs: https://www.figma.com/file/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?type=design&node-id=1032%3A44063&mode=design&t=rsNegTbEVLYAXL76-1

@Composable
fun DropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(x = 0.dp, y = 0.dp),
    properties: PopupProperties = PopupProperties(focusable = true),
    minWidth: Dp = DropdownMenuDefaults.minWidth,
    content: @Composable ColumnScope.() -> Unit
) {
    androidx.compose.material3.DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
            .background(color = ElementTheme.colors.bgCanvasDefaultLevel1)
            .widthIn(min = minWidth),
        shape = RoundedCornerShape(8.dp),
        offset = offset,
        properties = properties,
        content = content
    )
}

object DropdownMenuDefaults {
    val minWidth = 200.dp
}
