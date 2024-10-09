/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.roomselect.impl

import io.element.android.libraries.matrix.ui.model.SelectRoomInfo

sealed interface RoomSelectEvents {
    data class SetSelectedRoom(val room: SelectRoomInfo) : RoomSelectEvents

    // TODO remove to restore multi-selection
    data object RemoveSelectedRoom : RoomSelectEvents
    data object ToggleSearchActive : RoomSelectEvents
    data class UpdateQuery(val query: String) : RoomSelectEvents
}
