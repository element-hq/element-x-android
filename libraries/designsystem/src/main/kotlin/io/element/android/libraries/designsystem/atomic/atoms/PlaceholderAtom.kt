/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.atomic.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.placeholderBackground

@Composable
fun PlaceholderAtom(
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier,
    color: Color = ElementTheme.colors.placeholderBackground,
) {
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .background(
                color = color,
                shape = RoundedCornerShape(size = height / 2)
            )
    )
}

@PreviewsDayNight
@Composable
internal fun PlaceholderAtomPreview() = ElementPreview {
    // Use a Red background to see the shape
    Box(modifier = Modifier.background(color = Color.Red)) {
        PlaceholderAtom(
            width = 80.dp,
            height = 12.dp
        )
    }
}
