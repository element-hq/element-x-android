/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.roomselect.impl

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.ui.components.aSelectRoomInfo
import io.element.android.libraries.matrix.ui.model.SelectRoomInfo
import io.element.android.libraries.roomselect.api.RoomSelectMode
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

open class RoomSelectStateProvider : PreviewParameterProvider<RoomSelectState> {
    override val values: Sequence<RoomSelectState>
        get() = sequenceOf(
            aRoomSelectState(),
            aRoomSelectState(searchQuery = "Test", isSearchActive = true),
            aRoomSelectState(resultState = SearchBarResultState.Results(aRoomSelectRoomList())),
            aRoomSelectState(
                resultState = SearchBarResultState.Results(aRoomSelectRoomList()),
                searchQuery = "Test",
                isSearchActive = true,
            ),
            aRoomSelectState(
                resultState = SearchBarResultState.Results(aRoomSelectRoomList()),
                searchQuery = "Test",
                isSearchActive = true,
                selectedRooms = aRoomSelectRoomList().subList(0, 1),
            ),
            aRoomSelectState(
                mode = RoomSelectMode.Share,
                resultState = SearchBarResultState.Results(aRoomSelectRoomList()),
            ),
            aRoomSelectState(
                mode = RoomSelectMode.Share,
                resultState = SearchBarResultState.Results(aRoomSelectRoomList()),
                selectedRooms = aRoomSelectRoomList().subList(0, 1),
                maxNumberOfRooms = 1,
            ),
        )
}

internal fun aRoomSelectState(
    mode: RoomSelectMode = RoomSelectMode.Forward,
    maxNumberOfRooms: Int = 10,
    resultState: SearchBarResultState<ImmutableList<SelectRoomInfo>> = SearchBarResultState.Initial(),
    searchQuery: String = "",
    isSearchActive: Boolean = false,
    selectedRooms: ImmutableList<SelectRoomInfo> = persistentListOf(),
    eventSink: (RoomSelectEvent) -> Unit = {},
) = RoomSelectState(
    mode = mode,
    maxNumberOfRooms = maxNumberOfRooms,
    resultState = resultState,
    searchQuery = TextFieldState(initialText = searchQuery),
    isSearchActive = isSearchActive,
    selectedRooms = selectedRooms,
    eventSink = eventSink,
)

internal fun aRoomSelectRoomList() = persistentListOf(
    aSelectRoomInfo(
        roomId = RoomId("!room1:domain"),
        name = "Room with name",
    ),
    aSelectRoomInfo(
        roomId = RoomId("!room2:domain"),
        name = "Room with alias",
        canonicalAlias = RoomAlias("#alias:example.org"),
    ),
    aSelectRoomInfo(
        roomId = RoomId("!room3:domain"),
    ),
)
