/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.leave

import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class LeaveSpaceState(
    val spaceName: String?,
    val isLastAdmin: Boolean,
    val selectableSpaceRooms: AsyncData<ImmutableList<SelectableSpaceRoom>>,
    val leaveSpaceAction: AsyncAction<Unit>,
    val eventSink: (LeaveSpaceEvents) -> Unit,
) {
    private val rooms = selectableSpaceRooms.dataOrNull().orEmpty().toImmutableList()
    private val lastAdminRooms: ImmutableList<SelectableSpaceRoom>
    private val selectableRooms: ImmutableList<SelectableSpaceRoom>

    init {
        val partition = rooms.partition { it.isLastAdmin }
        lastAdminRooms = partition.first.toImmutableList()
        selectableRooms = partition.second.toImmutableList()
    }

    /**
     * True if we should show the quick action to select/deselect all rooms.
     */
    val showQuickAction = isLastAdmin.not() && selectableRooms.isNotEmpty()

    /**
     * True if we should show the leave button.
     */
    val showLeaveButton = isLastAdmin.not() && selectableSpaceRooms is AsyncData.Success

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
