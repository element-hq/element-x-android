/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DividerDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup

@Composable
fun HorizontalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = ElementDividerDefaults.thickness,
    color: Color = DividerDefaults.color,
) {
    androidx.compose.material3.HorizontalDivider(
        modifier = modifier,
        thickness = thickness,
        color = color,
    )
}

object ElementDividerDefaults {
    val thickness = 0.5.dp
}

@Preview(group = PreviewGroup.Dividers)
@Composable
internal fun HorizontalDividerPreview() = ElementThemedPreview {
    Box(Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
        HorizontalDivider()
    }
}
