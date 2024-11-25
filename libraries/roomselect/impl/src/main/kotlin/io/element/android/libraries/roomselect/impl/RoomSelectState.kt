/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.roomselect.impl

import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.ui.model.SelectRoomInfo
import io.element.android.libraries.roomselect.api.RoomSelectMode
import kotlinx.collections.immutable.ImmutableList

data class RoomSelectState(
    val mode: RoomSelectMode,
    val resultState: SearchBarResultState<ImmutableList<SelectRoomInfo>>,
    val query: String,
    val isSearchActive: Boolean,
    val selectedRooms: ImmutableList<SelectRoomInfo>,
    val eventSink: (RoomSelectEvents) -> Unit
)
