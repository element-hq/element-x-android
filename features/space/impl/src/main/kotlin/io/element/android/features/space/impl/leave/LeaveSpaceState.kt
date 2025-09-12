/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.leave

import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.core.bool.orFalse
import kotlinx.collections.immutable.ImmutableList

data class LeaveSpaceState(
    val spaceName: String?,
    val selectableSpaceRooms: AsyncData<ImmutableList<SelectableSpaceRoom>>,
    val leaveSpaceAction: AsyncAction<Unit>,
    val eventSink: (LeaveSpaceEvents) -> Unit,
) {
    val showQuickAction = selectableSpaceRooms.dataOrNull().orEmpty().any { !it.isLastAdmin }
    val hasOnlyLastAdminRoom = selectableSpaceRooms.dataOrNull()
        ?.let { rooms ->
            rooms.isNotEmpty() && rooms.all { it.isLastAdmin }
        }
        .orFalse()
    val numberOfSelectRooms = selectableSpaceRooms.dataOrNull().orEmpty().count { it.isSelected }

    val areAllSelected = selectableSpaceRooms.dataOrNull()
        ?.filter { !it.isLastAdmin }
        ?.let { rooms ->
            rooms.isNotEmpty() && rooms.all { it.isSelected }
        }
        .orFalse()
}
