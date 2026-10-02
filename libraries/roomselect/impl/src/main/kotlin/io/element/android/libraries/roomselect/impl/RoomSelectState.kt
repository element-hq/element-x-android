/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.roomselect.impl

import androidx.compose.foundation.text.input.TextFieldState
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.model.SelectRoomInfo
import io.element.android.libraries.roomselect.api.RoomSelectMode
import kotlinx.collections.immutable.ImmutableList

data class RoomSelectState(
    val mode: RoomSelectMode,
    val maxNumberOfRooms: Int,
    val resultState: SearchBarResultState<ImmutableList<SelectRoomInfo>>,
    val searchQuery: TextFieldState,
    val isSearchActive: Boolean,
    val selectedRooms: ImmutableList<SelectRoomInfo>,
    val selectedAccount: MatrixUser,
    val otherAccounts: ImmutableList<MatrixUser>,
    val isAccountListExpanded: Boolean,
    val eventSink: (RoomSelectEvent) -> Unit,
) {
    val canSelectMoreRooms = selectedRooms.size < maxNumberOfRooms

    /**
     * The account switch is only displayed when sharing, and if there are several accounts.
     */
    val showAccountSwitch = mode == RoomSelectMode.Share && otherAccounts.isNotEmpty()
}
