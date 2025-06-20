/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Creates and remembers an [ExpandableBottomSheetLayoutState].
 */
@Composable
fun rememberExpandableBottomSheetLayoutState(): ExpandableBottomSheetLayoutState {
    return remember { ExpandableBottomSheetLayoutState() }
}

/**
 * State for the [ExpandableBottomSheetLayout].
 *
 * This state holds the current position of the bottom sheet layout and the percentage of the layout that is being dragged.
 */
@Stable
class ExpandableBottomSheetLayoutState {
    internal var internalPosition: Position by mutableStateOf(Position.COLLAPSED)
    internal var internalDraggingPercentage: Float by mutableFloatStateOf(
        if (internalPosition == Position.EXPANDED) 1f else 0f
    )

    /**
     * The current position of the bottom sheet layout.
     */
    val position = internalPosition

    /**
     * The percentage of the bottom sheet layout that is currently being dragged.
     * This value ranges from `0f` for [Position.COLLAPSED] to `1f` for [Position.EXPANDED].
     */
    val draggingPercentage = internalDraggingPercentage

    /**
     * The position of the bottom sheet layout.
     */
    enum class Position {
        /** The bottom sheet is collapsed to its minimum visible height. */
        COLLAPSED,

        /** The bottom sheet is being dragged by user input. */
        DRAGGING,

        /** The bottom sheet is expanded to its maximum visible height. */
        EXPANDED
    }
}
