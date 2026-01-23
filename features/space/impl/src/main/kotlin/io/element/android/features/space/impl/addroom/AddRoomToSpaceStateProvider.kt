/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.addroom

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.ui.model.SelectRoomInfo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

internal class AddRoomToSpaceStateProvider : PreviewParameterProvider<AddRoomToSpaceState> {
    override val values: Sequence<AddRoomToSpaceState>
        get() = sequenceOf(
            // Initial state with suggestions
            anAddRoomToSpaceState(
                suggestions = aSelectRoomInfoList(),
            ),
            // Search active, empty query
            anAddRoomToSpaceState(
                isSearchActive = true,
                searchQuery = "",
                suggestions = aSelectRoomInfoList(),
            ),
            // Search active with query and results
            anAddRoomToSpaceState(
                isSearchActive = true,
                searchQuery = "general",
                searchResults = SearchBarResultState.Results(aSelectRoomInfoList()),
            ),
            // Search active with query and no results
            anAddRoomToSpaceState(
                isSearchActive = true,
                searchQuery = "unknown",
                searchResults = SearchBarResultState.NoResultsFound(),
            ),
            // With selected rooms
            anAddRoomToSpaceState(
                suggestions = aSelectRoomInfoList(),
                selectedRooms = aSelectRoomInfoList().take(1).toImmutableList(),
            ),
            // Loading state
            anAddRoomToSpaceState(
                selectedRooms = aSelectRoomInfoList().take(1).toImmutableList(),
                saveAction = AsyncAction.Loading,
            ),
            // Error state
            anAddRoomToSpaceState(
                selectedRooms = aSelectRoomInfoList().take(1).toImmutableList(),
                saveAction = AsyncAction.Failure(Exception("Failed to add rooms")),
            ),
        )
}

internal fun anAddRoomToSpaceState(
    searchQuery: String = "",
    searchResults: SearchBarResultState<ImmutableList<SelectRoomInfo>> = SearchBarResultState.Initial(),
    selectedRooms: ImmutableList<SelectRoomInfo> = persistentListOf(),
    isSearchActive: Boolean = false,
    saveAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    suggestions: ImmutableList<SelectRoomInfo> = persistentListOf(),
    eventSink: (AddRoomToSpaceEvent) -> Unit = {},
): AddRoomToSpaceState {
    return AddRoomToSpaceState(
        searchQuery = searchQuery,
        searchResults = searchResults,
        selectedRooms = selectedRooms,
        isSearchActive = isSearchActive,
        saveAction = saveAction,
        suggestions = suggestions,
        eventSink = eventSink,
    )
}

internal fun aSelectRoomInfoList(): ImmutableList<SelectRoomInfo> = listOf(
    SelectRoomInfo(
        roomId = RoomId("!room1:server.org"),
        name = "General",
        canonicalAlias = null,
        avatarUrl = null,
        heroes = persistentListOf(),
        isTombstoned = false,
    ),
    SelectRoomInfo(
        roomId = RoomId("!room2:server.org"),
        name = "Engineering",
        canonicalAlias = null,
        avatarUrl = null,
        heroes = persistentListOf(),
        isTombstoned = false,
    ),
    SelectRoomInfo(
        roomId = RoomId("!room3:server.org"),
        name = "Design",
        canonicalAlias = null,
        avatarUrl = null,
        heroes = persistentListOf(),
        isTombstoned = false,
    ),
).toImmutableList()
