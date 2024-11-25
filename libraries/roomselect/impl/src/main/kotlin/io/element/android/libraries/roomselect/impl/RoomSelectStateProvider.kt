/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.roomselect.impl

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
            aRoomSelectState(query = "Test", isSearchActive = true),
            aRoomSelectState(resultState = SearchBarResultState.Results(aRoomSelectRoomList())),
            aRoomSelectState(
                resultState = SearchBarResultState.Results(aRoomSelectRoomList()),
                query = "Test",
                isSearchActive = true,
            ),
            aRoomSelectState(
                resultState = SearchBarResultState.Results(aRoomSelectRoomList()),
                query = "Test",
                isSearchActive = true,
                selectedRooms = aRoomSelectRoomList().subList(0, 1),
            ),
            aRoomSelectState(
                mode = RoomSelectMode.Share,
                resultState = SearchBarResultState.Results(aRoomSelectRoomList()),
            ),
        )
}

private fun aRoomSelectState(
    mode: RoomSelectMode = RoomSelectMode.Forward,
    resultState: SearchBarResultState<ImmutableList<SelectRoomInfo>> = SearchBarResultState.Initial(),
    query: String = "",
    isSearchActive: Boolean = false,
    selectedRooms: ImmutableList<SelectRoomInfo> = persistentListOf(),
) = RoomSelectState(
    mode = mode,
    resultState = resultState,
    query = query,
    isSearchActive = isSearchActive,
    selectedRooms = selectedRooms,
    eventSink = {}
)

private fun aRoomSelectRoomList() = persistentListOf(
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
