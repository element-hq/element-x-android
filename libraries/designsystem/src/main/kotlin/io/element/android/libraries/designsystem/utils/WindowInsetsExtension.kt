/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.utils

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection

@Composable
@ReadOnlyComposable
fun WindowInsets.copy(
    top: Int? = null,
    right: Int? = null,
    bottom: Int? = null,
    left: Int? = null
): WindowInsets {
    val density = LocalDensity.current
    val direction = LocalLayoutDirection.current
    return WindowInsets(
        top = top ?: this.getTop(density),
        right = right ?: this.getRight(density, direction),
        bottom = bottom ?: this.getBottom(density),
        left = left ?: this.getLeft(density, direction)
    )
}
