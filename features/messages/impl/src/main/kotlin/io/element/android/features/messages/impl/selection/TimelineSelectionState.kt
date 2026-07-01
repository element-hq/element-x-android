/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.selection

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import io.element.android.libraries.matrix.api.core.EventId
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableSet

/**
 * State of the optional bulk-message selection mode.
 * `isActive` is true while the user is multi-selecting; tap on a message in this mode
 * toggles its membership in [selectedIds] instead of opening the single-message action sheet.
 */
@Immutable
data class TimelineSelectionState(
    val isActive: Boolean,
    val selectedIds: ImmutableSet<EventId>,
    val maxSelection: Int,
) {
    val count: Int get() = selectedIds.size
    val isAtCap: Boolean get() = count >= maxSelection

    companion object {
        const val MAX_SELECTION = 30

        /** Inactive selection with nothing selected. */
        val Empty = TimelineSelectionState(
            isActive = false,
            selectedIds = persistentSetOf(),
            maxSelection = MAX_SELECTION,
        )

        /**
         * Persists isActive + maxSelection + the selected event-id strings so a large in-progress
         * selection survives configuration changes and process death.
         */
        val Saver: Saver<TimelineSelectionState, Any> = listSaver(
            save = { state ->
                buildList {
                    add(state.isActive.toString())
                    add(state.maxSelection.toString())
                    state.selectedIds.forEach { add(it.value) }
                }
            },
            restore = { stored ->
                // Defensive: a malformed payload restores an empty selection rather than crashing.
                if (stored.size < 2) {
                    Empty
                } else {
                    TimelineSelectionState(
                        isActive = stored[0].toBoolean(),
                        maxSelection = stored[1].toIntOrNull() ?: MAX_SELECTION,
                        selectedIds = stored.drop(2).map(::EventId).toImmutableSet(),
                    )
                }
            },
        )
    }
}
