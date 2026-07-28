/*
 * Copyright (c) 2026 Element Creations Ltd.
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
 * State of the bulk-message selection mode, active as soon as [selectedIds] is not empty.
 */
@Immutable
data class TimelineSelectionState(
    val isEnabled: Boolean,
    val selectedIds: ImmutableSet<EventId>,
    val canDelete: Boolean,
) {
    val isActive: Boolean get() = selectedIds.isNotEmpty()
    val count: Int get() = selectedIds.size
    val isAtCap: Boolean get() = count >= MAX_SELECTION

    companion object {
        const val MAX_SELECTION = 30

        val Empty = TimelineSelectionState(
            isEnabled = false,
            selectedIds = persistentSetOf(),
            canDelete = false,
        )
    }
}

/**
 * Persists the selection across configuration changes and process death.
 */
val TimelineSelectionSaver: Saver<ImmutableSet<EventId>, Any> = listSaver(
    save = { selectedIds -> selectedIds.map { it.value } },
    restore = { stored -> stored.map(::EventId).toImmutableSet() },
)
