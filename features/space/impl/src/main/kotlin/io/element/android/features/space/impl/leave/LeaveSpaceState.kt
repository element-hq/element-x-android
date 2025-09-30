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
    private val partition = rooms.partition { it.isLastAdmin }
    private val lastAdminRooms = partition.first
    private val selectableRooms = partition.second

    /**
     * True if we should show the quick action to select/deselect all rooms.
     */
    val showQuickAction = selectableRooms.isNotEmpty()

    /**
     * True if there all the selectable rooms are selected.
     */
    val areAllSelected = selectableRooms.all { it.isSelected }

    /**
     * True if there are rooms but the user is the last admin in all of them.
     */
    val hasOnlyLastAdminRoom = lastAdminRooms.isNotEmpty() && selectableRooms.isEmpty()

    /**
     * Number of selected rooms.
     */
    val selectedRoomsCount = selectableRooms.count { it.isSelected }
}
