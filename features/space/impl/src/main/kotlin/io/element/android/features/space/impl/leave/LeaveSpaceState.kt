/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.leave

import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import kotlinx.collections.immutable.ImmutableList

data class LeaveSpaceState(
    val spaceName: String?,
    val selectableSpaceRooms: AsyncData<ImmutableList<SelectableSpaceRoom>>,
    val leaveSpaceAction: AsyncAction<Unit>,
    val eventSink: (LeaveSpaceEvents) -> Unit,
) {
    private val rooms = selectableSpaceRooms.dataOrNull().orEmpty()

    /**
     * True if we should show the quick action to select/deselect all rooms.
     */
    val showQuickAction = rooms
        .any { !it.isLastAdmin }

    /**
     * True if there are rooms and they are all selected.
     */
    val areAllSelected = rooms
        .filter { !it.isLastAdmin }
        .let { rooms ->
            rooms.isNotEmpty() && rooms.all { it.isSelected }
        }

    /**
     * True if there are rooms but the user is the last admin in all of them.
     */
    val hasOnlyLastAdminRoom = rooms
        .let { rooms ->
            rooms.isNotEmpty() && rooms.all { it.isLastAdmin }
        }

    /**
     * Number of selected rooms.
     */
    val numberOfSelectRooms = rooms
        .count { it.isSelected }
}
