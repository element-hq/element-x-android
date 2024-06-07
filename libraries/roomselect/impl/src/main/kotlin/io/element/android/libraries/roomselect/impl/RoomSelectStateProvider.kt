/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.roomselect.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.roomlist.RoomSummaryDetails
import io.element.android.libraries.matrix.ui.components.aRoomSummaryDetails
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
                selectedRooms = persistentListOf(aRoomSummaryDetails(roomId = RoomId("!room2:domain")))
            ),
            aRoomSelectState(
                mode = RoomSelectMode.Share,
                resultState = SearchBarResultState.Results(aRoomSelectRoomList()),
            ),
        )
}

private fun aRoomSelectState(
    mode: RoomSelectMode = RoomSelectMode.Forward,
    resultState: SearchBarResultState<ImmutableList<RoomSummaryDetails>> = SearchBarResultState.Initial(),
    query: String = "",
    isSearchActive: Boolean = false,
    selectedRooms: ImmutableList<RoomSummaryDetails> = persistentListOf(),
) = RoomSelectState(
    mode = mode,
    resultState = resultState,
    query = query,
    isSearchActive = isSearchActive,
    selectedRooms = selectedRooms,
    eventSink = {}
)

private fun aRoomSelectRoomList() = persistentListOf(
    aRoomSummaryDetails(),
    aRoomSummaryDetails(
        roomId = RoomId("!room2:domain"),
        name = "Room with alias",
        canonicalAlias = RoomAlias("#alias:example.org"),
    ),
    aRoomSummaryDetails(
        roomId = RoomId("!room3:domain"),
        name = null,
    ),
)
