/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.modifiers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * This modifier can be use to provide a nice background for Icon or ProgressIndicator.
 */
fun Modifier.roundedBackground(
    size: Dp = 48.dp,
    color: Color = Color.Black,
    alpha: Float = 0.5f,
) = this
    .size(size)
    .clip(CircleShape)
    .background(color = color.copy(alpha = alpha))
    .padding(8.dp)
