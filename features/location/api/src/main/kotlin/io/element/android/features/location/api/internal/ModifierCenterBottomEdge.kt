/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.api.internal

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset

/**
 * Horizontally aligns the content to the center of the space.
 * Vertically aligns the bottom edge of the content to the center of the space.
 */
fun Modifier.centerBottomEdge(scope: BoxScope): Modifier = this.then(
    with(scope) {
        Modifier.align { size, space, _ ->
            IntOffset(
                x = (space.width - size.width) / 2,
                y = space.height / 2 - size.height,
            )
        }
    }
)
