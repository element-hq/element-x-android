/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.selection

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.core.EventId
import kotlinx.collections.immutable.toImmutableSet

open class TimelineSelectionStateProvider : PreviewParameterProvider<TimelineSelectionState> {
    override val values: Sequence<TimelineSelectionState>
        get() = sequenceOf(
            aTimelineSelectionState(),
            aTimelineSelectionState(canDelete = false),
            aTimelineSelectionState(count = TimelineSelectionState.MAX_SELECTION),
        )
}

fun aTimelineSelectionState(
    count: Int = 3,
    canDelete: Boolean = true,
) = TimelineSelectionState(
    isEnabled = true,
    selectedIds = (1..count).map { EventId("\$selected-$it") }.toImmutableSet(),
    canDelete = canDelete,
)
